package lila.tournament

import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import scala.concurrent.duration._

import lila.common.LightUser
import lila.common.PimpedJson._
import lila.game.{ Game, GameRepo }
import lila.user.User

final class JsonView(
    getLightUser: String => Option[LightUser]) {

  private case class CachableData(pairings: JsArray, games: JsArray, podium: Option[JsArray])

  def apply(tour: Tournament, page: Option[Int], me: Option[String]): Fu[JsObject] = for {
    data <- cachableData(tour.id)
    myInfo <- me ?? { PlayerRepo.playerInfo(tour.id, _) }
    stand <- (myInfo, page) match {
      case (_, Some(p)) => standing(tour, p)
      case (Some(i), _) => standing(tour, i.page)
      case _            => standing(tour, 1)
    }
  } yield Json.obj(
    "id" -> tour.id,
    "createdBy" -> tour.createdBy,
    "system" -> tour.system.toString.toLowerCase,
    "fullName" -> tour.fullName,
    "nbPlayers" -> tour.nbPlayers,
    "private" -> tour.`private`.option(true),
    "variant" -> tour.variant.key,
    "isStarted" -> tour.isStarted,
    "isFinished" -> tour.isFinished,
    "schedule" -> tour.schedule.map(scheduleJson),
    "secondsToFinish" -> tour.isStarted.option(tour.secondsToFinish),
    "secondsToStart" -> tour.isCreated.option(tour.secondsToStart),
    "startsAt" -> tour.isCreated.option(ISODateTimeFormat.dateTime.print(tour.startsAt)),
    "pairings" -> data.pairings,
    "lastGames" -> data.games,
    "standing" -> stand,
    "me" -> myInfo.map(myInfoJson),
    "podium" -> data.podium
  ).noNull

  def standing(tour: Tournament, page: Int): Fu[JsObject] =
    if (page == 1) firstPageCache(tour.id)
    else computeStanding(tour, page)

  def clearCache(id: String) =
    firstPageCache.remove(id) >> cachableData.remove(id)

  private def computeStanding(tour: Tournament, page: Int): Fu[JsObject] = for {
    rankedPlayers <- PlayerRepo.bestByTourWithRankByPage(tour.id, 10, page max 1)
    sheets <- rankedPlayers.map { p =>
      tour.system.scoringSystem.sheet(tour, p.player.userId) map p.player.userId.->
    }.sequenceFu.map(_.toMap)
  } yield Json.obj(
    "page" -> page,
    "players" -> rankedPlayers.map(playerJson(sheets, tour))
  )

  private val firstPageCache = lila.memo.AsyncCache[String, JsObject](
    (id: String) => TournamentRepo byId id flatten s"No such tournament: $id" flatMap { computeStanding(_, 1) },
    timeToLive = 1 second)

  private val cachableData = lila.memo.AsyncCache[String, CachableData](id =>
    for {
      pairings <- PairingRepo.recentByTour(id, 40)
      games <- GameRepo games pairings.take(4).map(_.gameId)
      podium <- podiumJson(id)
    } yield CachableData(
      JsArray(pairings map pairingJson),
      JsArray(games map gameJson),
      podium),
    timeToLive = 1 second)

  private def myInfoJson(i: PlayerInfo) = Json.obj(
    "rank" -> i.rank,
    "withdraw" -> i.withdraw)

  private def gameUserJson(player: lila.game.Player) = {
    val light = player.userId flatMap getLightUser
    Json.obj(
      "name" -> light.map(_.name),
      "title" -> light.map(_.title),
      "rating" -> player.rating
    ).noNull
  }

  private def gameJson(g: Game) = Json.obj(
    "id" -> g.id,
    "fen" -> (chess.format.Forsyth exportBoard g.toChess.board),
    "color" -> g.firstColor.name,
    "lastMove" -> ~g.castleLastMoveTime.lastMoveString,
    "user1" -> gameUserJson(g.firstPlayer),
    "user2" -> gameUserJson(g.secondPlayer))

  private def scheduleJson(s: Schedule) = Json.obj(
    "freq" -> s.freq.name,
    "speed" -> s.speed.name)

  private def sheetJson(sheet: ScoreSheet) = sheet match {
    case s: arena.ScoringSystem.Sheet =>
      val o = Json.obj(
        "scores" -> s.scores.reverse.map { score =>
          if (score.flag == arena.ScoringSystem.Normal) JsNumber(score.value)
          else Json.arr(score.value, score.flag.id)
        },
        "total" -> s.total)
      s.onFire.fold(o + ("fire" -> JsBoolean(true)), o)
  }

  private def playerJson(sheets: Map[String, ScoreSheet], tour: Tournament)(rankedPlayer: RankedPlayer) = {
    val p = rankedPlayer.player
    val light = getLightUser(p.userId)
    Json.obj(
      "rank" -> rankedPlayer.rank,
      "name" -> light.fold(p.userId)(_.name),
      "title" -> light.map(_.title),
      "rating" -> p.rating,
      "provisional" -> p.provisional.option(true),
      "withdraw" -> p.withdraw.option(true),
      "score" -> p.score,
      "perf" -> p.perf,
      // "opposition" -> none[Int], //(tour.isFinished && rankedPlayer.rank <= 3).option(opposition(tour, p)),
      "sheet" -> sheets.get(p.userId).map(sheetJson)
    ).noNull
  }

  private def podiumJson(id: String): Fu[Option[JsArray]] =
    TournamentRepo finishedById id flatMap {
      _ ?? { tour =>
        for {
          rankedPlayers <- PlayerRepo.bestByTourWithRank(id, 3)
          sheets <- rankedPlayers.map { p =>
            tour.system.scoringSystem.sheet(tour, p.player.userId) map p.player.userId.->
          }.sequenceFu.map(_.toMap)
        } yield JsArray(rankedPlayers.map(playerJson(sheets, tour))).some
      }
    }

  // private def opposition(tour: Tournament, p: Player): Int =
  //   tour.userPairings(p.id).foldLeft((0, 0)) {
  //     case ((count, sum), pairing) => (
  //       count + 1,
  //       (pairing opponentOf p.id flatMap tour.playerByUserId).fold(sum)(_.rating + sum)
  //     )
  //   } match {
  //     case (0, _)       => 0
  //     case (count, sum) => sum / count
  //   }

  private def pairingUserJson(userId: String) = getLightUser(userId).fold(userId)(_.name)

  private def pairingJson(p: Pairing) = Json.obj(
    "id" -> p.gameId,
    "u" -> Json.arr(pairingUserJson(p.user1), pairingUserJson(p.user2)),
    "s" -> (if (p.finished) p.winner match {
      case Some(w) if w == p.user1 => 2
      case Some(w)                 => 3
      case _                       => 1
    }
    else 0))
}

package lila.team

import akka.actor.PoisonPill
import lila.common.LightUser
import lila.hub.TimeBomb
import lila.socket.actorApi.{Quit, GetVersion, Broom, PingVersion}
import lila.socket.{Historical, SocketActor, History}
import lila.team.actorApi.Messadata
import lila.team.actorApi._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import scala.concurrent.duration._

private[team] final class Socket(teamId: String,
                                 val history: History[Messadata],
                                 lightUser: String => Option[LightUser],
                                 uidTimeout: Duration,
                                 socketTimeout: Duration) extends SocketActor[LiveMember](uidTimeout)
                                                          with Historical[LiveMember, Messadata] {

  private val timeBomb = new TimeBomb(socketTimeout)
  private var delayedCrowdNotification = false

  def receiveSpecific = {

    case lila.chat.actorApi.ChatLine(_, line) => line match {
      case line: lila.chat.UserLine =>
        notifyVersion("message", lila.chat.Line toJson line, Messadata(line.troll))
      case _ =>
    }

    case PingVersion(uid, v) => {
      ping(uid)
      timeBomb.delay
      withMember(uid) { m =>
        history.since(v).fold(resync(m))(_ foreach sendMessage(m))
      }
    }

    case Broom => {
      broom
      if (timeBomb.boom) self ! PoisonPill
    }

    case lila.chat.actorApi.ChatLine(_, line) => line match {
      case line: lila.chat.UserLine =>
        notifyVersion("message", lila.chat.Line toJson line, Messadata(line.troll))
      case _ =>
    }

    case GetVersion => sender ! history.version

    case Join(uid, user, version) =>
      val (enumerator, channel) = Concurrent.broadcast[JsValue]
      val member = LiveMember(channel, user)
      addMember(uid, member)
      notifyCrowd
      sender ! Connected(enumerator, member)

    case Quit(uid) =>
      quit(uid)
      notifyCrowd

    case NotifyCrowd =>
      delayedCrowdNotification = false
      val (anons, users) = members.values.map(_.userId flatMap lightUser).foldLeft(0 -> List[LightUser]()) {
        case ((anons, users), Some(user)) => anons -> (user :: users)
        case ((anons, users), None) => (anons + 1) -> users
      }
      notifyVersion("crowd", showSpectators(users, anons), Messadata())
  }

  def notifyCrowd {
    if (!delayedCrowdNotification) {
      delayedCrowdNotification = true
      context.system.scheduler.scheduleOnce(500 millis, self, NotifyCrowd)
    }
  }

  override protected def shouldSkipMessageFor(message: Message, member: LiveMember): Boolean =
    message.metadata.trollish && !member.troll

}

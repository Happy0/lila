package lila.team

import actorApi._
import akka.pattern.ask
import akka.actor.{ActorSelection, ActorRef}
import lila.hub.actorApi.map.Get
import lila.security.Flood
import lila.socket.Handler
import lila.user.User
import lila.socket.actorApi.{Connected => _, _}

private[team] class SocketHandler(
                                   hub: lila.hub.Env,
                                   socketHub: ActorRef,
                                   chat: ActorSelection,
                                   flood: Flood) {

  def join(
            teamId: String,
            version: Int,
            uid: String,
            user: Option[User]): Fu[Option[JsSocketHandler]] =
    TeamRepo.exists(teamId) flatMap {
      _ ?? {
        for {
          socket ← socketHub ? Get(teamId) mapTo manifest[ActorRef]
          join = Join(uid = uid, user = user, version = version)
          handler ← Handler(hub, socket, uid, join, user map (_.id)) {
            case Connected(enum, member) =>
              (controller(socket, teamId, uid, member), enum, member)
          }
        } yield handler.some
      }
    }


  private def controller(socket: ActorRef,
                         teamId: String,
                         uid: String,
                         member: LiveMember): Handler.Controller = {
    case _ =>
  }


}

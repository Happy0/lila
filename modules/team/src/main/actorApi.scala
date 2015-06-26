package lila.team
package actorApi

import lila.socket.SocketMember
import lila.tournament._
import lila.user.User

private[team] case class LiveMember (
                                 channel: JsChannel,
                                 userId: Option[String],
                                 troll: Boolean) extends SocketMember
private[team] object Member {
  def apply(channel: JsChannel, user: Option[User]): LiveMember = LiveMember (
    channel = channel,
    userId = user map (_.id),
    troll = user.??(_.troll))
}

private[team] case class Join(
                                     uid: String,
                                     user: Option[User],
                                     version: Int)

private[team] case class Connected(enumerator: JsEnumerator, member: LiveMember)

case class InsertTeam(team: Team)
case class RemoveTeam(id: String)

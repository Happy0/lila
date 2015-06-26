package lila.team
package actorApi

import lila.socket.SocketMember
import lila.user.User

private[team] case class Member (
                                 channel: JsChannel,
                                 userId: Option[String],
                                 troll: Boolean) extends SocketMember
private[team] object Member {
  def apply(channel: JsChannel, user: Option[User]): Member = Member (
    channel = channel,
    userId = user map (_.id),
    troll = user.??(_.troll))
}

case class InsertTeam(team: Team)
case class RemoveTeam(id: String)

package lila.notify

import lila.db.dsl._
import lila.hub.actorApi.notify.Notification

private final class NotificationRepo(coll: Coll) {

  import BSONHandlers._

  def insert(notification: Notification) : Funit = {
    coll.insert(notification).void
  }

  def unreadNotificationsCount(userId: Notification.Notifies) : Fu[Int] = {
    coll.count(unreadOnlyQuery(userId).some)
  }

  private def userNotificationsQuery(userId: Notification.Notifies) = $doc("userId" -> userId.value)

  private def unreadOnlyQuery(userId:Notification.Notifies) = $doc("userId" -> userId.value, "read" -> "false")

}
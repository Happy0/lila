package lila.notify

import lila.common.paginator.Paginator
import lila.hub.actorApi.SendTo

private[notify] final class Api(bus: lila.common.Bus) {

  def getNotifications(userId: String) : Fu[Paginator[Notification]] = ???

  def addNotification(userId: String, notification: Notification) = {
    // Add to database

    val numUnreadNotifications = 1
    // Notify client of new notification
    notifyUserIfLoggedIn(userId, numUnreadNotifications)
  }

  def notifyUserIfLoggedIn(userId: String, unreadNotifications: Int) = {
    val notificationsEventKey = "newNotifications"
    val notificationEvent = SendTo(userId, notificationsEventKey, unreadNotifications)

    bus.publish(notificationEvent, 'Users)

  }


}
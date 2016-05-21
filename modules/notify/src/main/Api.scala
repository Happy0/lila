package lila.notify

import lila.common.paginator.Paginator
import lila.hub.actorApi.SendTo
import lila.hub.actorApi.notify.Notification

private[notify] final class Api(bus: lila.common.Bus) {

  def getNotifications(userId: String) : Fu[Paginator[Notification]] = ???

  def addNotification(userId: String, notification: Notification) = {
    // Add to database

    val numUnreadNotifications = 1
    // Notify client of new notification
    notifyUserIfLoggedIn(userId, numUnreadNotifications)
  }

  def notifyUserIfLoggedIn(userId: String, notification: Notification) = {
    val notificationsEventKey = "newNotification"
    val notificationEvent = SendTo(userId, notificationsEventKey, notification)
    bus.publish(notificationEvent, 'Users)
  }


}
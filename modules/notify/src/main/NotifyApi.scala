package lila.notify

import lila.common.paginator.Paginator
import lila.hub.actorApi.SendTo
import lila.hub.actorApi.notify.Notification

final class NotifyApi(bus: lila.common.Bus) {

  def getNotifications(userId: String) : Fu[Paginator[Notification]] = ???

  def addNotification(userId: String, notification: Notification) = {
    // Add to database

    // Notify client of new notification
    notifyUserIfLoggedIn(userId, notification)
  }

  def notifyUserIfLoggedIn(userId: String, notification: Notification) = {
    val notificationsEventKey = "newNotification"
    val notificationEvent = SendTo(userId, notificationsEventKey, notification)
    bus.publish(notificationEvent, 'Users)
  }


}
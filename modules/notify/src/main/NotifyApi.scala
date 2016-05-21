package lila.notify

import lila.common.paginator.Paginator
import lila.hub.actorApi.SendTo
import lila.hub.actorApi.notify.{NewNotification, Notification}

final class NotifyApi(bus: lila.common.Bus) {

  def getNotifications(userId: String) : Fu[Paginator[Notification]] = ???

  def addNotification(userId: String, notification: Notification) = {
    // Add to database

    val unreadNotifications = 1
    val newNotification = NewNotification(notification, unreadNotifications)

    // Notify client of new notification
    notifyConnectedClients(userId, newNotification)
  }

  def notifyConnectedClients(userId: String, notification: NewNotification) = {

    val notificationsEventKey = "new_notification"
    val notificationEvent = SendTo(userId, notificationsEventKey, notification)
    bus.publish(notificationEvent, 'users)
  }


}
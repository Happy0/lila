package lila.notify

import lila.common.paginator.Paginator
import lila.hub.actorApi.SendTo
import lila.hub.actorApi.notify.{NewNotification, Notification}

final class NotifyApi(bus: lila.common.Bus, repo: NotificationRepo) {

  def getNotifications(userId: String) : Fu[Paginator[Notification]] = ???

  def addNotification(notification: Notification) = {
    // Add to database

    val unreadNotifications = 1
    val newNotification = NewNotification(notification, unreadNotifications)

    // Notify client of new notification
    notifyConnectedClients(newNotification)
  }

  def notifyConnectedClients(newNotification: NewNotification) = {

    val notificationsEventKey = "new_notification"
    val notificationEvent = SendTo(newNotification.notification.notifies.value, notificationsEventKey, newNotification)
    bus.publish(notificationEvent, 'users)
  }


}
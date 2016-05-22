package lila.notify

import lila.common.paginator.Paginator
import lila.hub.actorApi.SendTo
import lila.hub.actorApi.notify.{NewNotification, Notification}

final class NotifyApi(bus: lila.common.Bus, repo: NotificationRepo) {

  def getNotifications(userId: Notification.Notifies, numNotifications: Int) : Fu[Paginator[Notification]] = ???

  def getUnseenNotificationCount(userId: Notification.Notifies): Fu[Int] = repo.unreadNotificationsCount(userId)

  def addNotification(notification: Notification): Funit = {

    val unreadNotifications = 1
    val newNotification = NewNotification(notification, unreadNotifications)

    // Add to database and then notify any connected clients of the new notification
    repo.insert(notification).map(_ => notifyConnectedClients(newNotification))
  }

  private def notifyConnectedClients(newNotification: NewNotification) = {

    val notificationsEventKey = "new_notification"
    val notificationEvent = SendTo(newNotification.notification.notifies.value, notificationsEventKey, newNotification)
    bus.publish(notificationEvent, 'users)
  }
}
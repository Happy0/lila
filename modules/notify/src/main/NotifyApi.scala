package lila.notify

import lila.common.paginator.Paginator
import lila.db.dsl._
import lila.db.paginator.Adapter
import lila.hub.actorApi.SendTo
import lila.hub.actorApi.notify.{NewNotification, Notification}

final class NotifyApi(bus: lila.common.Bus, repo: NotificationRepo) {

  import BSONHandlers.NotificationBSONHandler

  def getNotifications(userId: Notification.Notifies, page: Int, perPage: Int) : Fu[Paginator[Notification]] = Paginator(
    adapter = new Adapter(
      collection = repo.coll,
      selector = repo.userNotificationsQuery(userId),
      projection = $empty,
      sort = repo.recentSort),
    currentPage = page,
    maxPerPage = perPage
  )

  def getUnseenNotificationCount(userId: Notification.Notifies): Fu[Int] = repo.unreadNotificationsCount(userId)

  def addNotification(notification: Notification): Funit = {

    // Add to database and then notify any connected clients of the new notification
    repo.insert(notification) >>
      getUnseenNotificationCount(notification.notifies).map(NewNotification(notification, _)).map(notifyConnectedClients)
  }

  private def notifyConnectedClients(newNotification: NewNotification) : Unit = {
    val notificationsEventKey = "new_notification"
    val notificationEvent = SendTo(newNotification.notification.notifies.value, notificationsEventKey, newNotification)
    bus.publish(notificationEvent, 'users)
  }
}
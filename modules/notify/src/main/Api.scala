package lila.notify

import lila.common.paginator.Paginator

private[notify] final class Api() {

  def getNotifications(userId: String) : Fu[Paginator[Notification]] = ???

  def addNotification(userId: String, notification: Notification) = ???
}
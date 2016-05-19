package lila.notif

import lila.common.paginator.Paginator

private[notif] final class Api() {

  def getNotifications : Fu[Paginator[Notification]] = ???
}
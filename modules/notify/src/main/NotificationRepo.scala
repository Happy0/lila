package lila.notify

import lila.db.dsl._
import lila.hub.actorApi.notify.Notification

private final class NotificationRepo(coll: Coll) {

  def insert(notification: Notification) = {
    coll.insert(notification)
  }


}
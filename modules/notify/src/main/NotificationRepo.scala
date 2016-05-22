package lila.notify

import lila.db.dsl._
import lila.hub.actorApi.notify.Notification

private final class NotificationRepo(coll: Coll) {

  import BSONHandlers._

  def insert(notification: Notification) = {
    coll.insert(notification)
  }


}
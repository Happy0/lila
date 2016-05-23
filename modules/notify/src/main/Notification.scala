package lila.notify


import org.joda.time.DateTime
import ornicar.scalalib.Random
import play.api.libs.json.{JsValue, Json, Writes}

case class NewNotification(notification: Notification, unreadNotifications: Int)

case class Notification(_id: String, notifies: Notification.Notifies, content: NotificationContent, read: Notification.NotificationRead, createdAt: DateTime) {
  def id = _id
}

object Notification {

  case class Notifies(value: String) extends AnyVal with StringValue
  case class NotificationRead(value: Boolean)

  def apply(notifies: Notification.Notifies, content: NotificationContent, read: NotificationRead, createdAt: DateTime) : Notification = {
    val idSize = 8
    val id = Random nextStringUppercase idSize
    new Notification(id, notifies, content, read, createdAt)
  }
}

sealed trait NotificationContent
case class MentionedInThread(mentionedBy: MentionedInThread.MentionedBy,
                             topic: MentionedInThread.Topic, category: MentionedInThread.Category) extends NotificationContent

object MentionedInThread {
  case class MentionedBy(value: String) extends AnyVal with StringValue
  case class Topic(value: String) extends  AnyVal with StringValue
  case class Category(value: String) extends AnyVal with StringValue
}

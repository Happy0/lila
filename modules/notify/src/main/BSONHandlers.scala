package lichess.notif

import lila.db.{dsl, BSON}
import lila.db.BSON.Writer
import lila.hub.actorApi.notify.{MentionedInThread, Notification, NotificationContent}
import lila.db.BSON.{ Reader, Writer }
import lila.db.dsl._

private object BSONHandlers {

  implicit val NotificationContentHandler = new BSON[NotificationContent] {

    private def writeNotificationType(notificationContent: NotificationContent) = {
      notificationContent match {
        case MentionedInThread(mentionedBy, topic, category) => "mention"

      }
    }

    private def writeNotificationContent(notificationContent: NotificationContent) = {
      notificationContent match {
        case MentionedInThread(mentionedBy, topic, category) =>
          $doc("mentionedBy" -> mentionedBy.value, "topic" -> topic.value, "category" -> category.value)
      }
    }

    override def reads(reader: Reader): NotificationContent = ???

    override def writes(writer: Writer, n: NotificationContent): dsl.Bdoc = {
      $doc("type" -> writeNotificationType(n), "content" -> writeNotificationContent(n))
    }
  }

  implicit val NotificationBSONHandler = new BSON[Notification] {
    override def reads(reader: Reader): Notification = ???

    override def writes(writer: Writer, n: Notification): dsl.Bdoc = $doc(
      "id" -> n.id,
      "created" -> n.createdAt,
      "read" -> n.read.value,
      "notifies" -> n.notifies.value,
      "content" -> n.content
    )
  }
}
package lila.notify

import lila.db.{dsl, BSON}
import lila.hub.actorApi.notify.MentionedInThread.{Category, Topic, MentionedBy}
import lila.hub.actorApi.notify.Notification.{Notifies, NotificationRead}
import lila.hub.actorApi.notify.{MentionedInThread, Notification, NotificationContent}
import lila.db.BSON.{ Reader, Writer }
import lila.db.dsl._
import reactivemongo.bson.BSONDocument

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
          $doc("type" -> writeNotificationType(notificationContent), "mentionedBy" -> mentionedBy.value,
            "topic" -> topic.value, "category" -> category.value)
      }
    }

    private def readMentionedNotification(reader: Reader) : MentionedInThread = {
      val mentionedBy = MentionedBy(reader.str("mentionedBy"))
      val topic = Topic(reader.str("topic"))
      val category = Category(reader.str("category"))

      MentionedInThread(mentionedBy, topic, category)
    }

    override def reads(reader: Reader): NotificationContent = {
      val notificationType = reader.str("type")

      notificationType match {
        case "mention" => readMentionedNotification(reader)
      }
    }

    override def writes(writer: Writer, n: NotificationContent): dsl.Bdoc = {
      writeNotificationContent(n)
    }
  }

  implicit val NotificationBSONHandler = new BSON[Notification] {
    override def reads(reader: Reader): Notification = {
      val id = reader.str("_id")
      val created = reader.date("created")
      val hasRead = NotificationRead(reader.bool("read"))
      val notifies = Notifies(reader.str("notifies"))
      val content = reader.get[NotificationContent]("content")

      Notification(id, notifies, content, hasRead, created)
    }

    override def writes(writer: Writer, n: Notification): dsl.Bdoc = $doc(
      "_id" -> n.id,
      "created" -> n.createdAt,
      "read" -> n.read.value,
      "notifies" -> n.notifies.value,
      "content" -> n.content
    )
  }
}
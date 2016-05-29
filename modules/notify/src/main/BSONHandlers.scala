package lila.notify

import lila.db.{dsl, BSON}
import lila.db.BSON.{Reader, Writer}
import lila.db.dsl._
import lila.notify.MentionedInThread._
import lila.notify.Notification._
import reactivemongo.bson.{BSONString, BSONHandler, BSONDocument}

private object BSONHandlers {

  implicit val MentionByHandler = stringAnyValHandler[MentionedBy](_.value, MentionedBy.apply)

  implicit val TopicHandler = stringAnyValHandler[Topic](_.value, Topic.apply)

  implicit val CategoryHandler = stringAnyValHandler[Category](_.value, Category.apply)

  implicit val PostIdHandler = stringAnyValHandler[PostId](_.value, PostId.apply)

  implicit val NotificationContentHandler = new BSON[NotificationContent] {

    private def writeNotificationType(notificationContent: NotificationContent) = {
      notificationContent match {
        case MentionedInThread(_y, _, _, _) => "mention"
      }
    }

    private def writeNotificationContent(notificationContent: NotificationContent) = {
      notificationContent match {
        case MentionedInThread(mentionedBy, topic, category, postId) =>
          $doc("type" -> writeNotificationType(notificationContent), "mentionedBy" -> mentionedBy,
            "topic" -> topic, "category" -> category, "postId" -> postId)
      }
    }

    private def readMentionedNotification(reader: Reader): MentionedInThread = {
      val mentionedBy = reader.get[MentionedBy]("mentionedBy")
      val topic = reader.get[Topic]("topic")
      val category = reader.get[Category]("category")
      val postNumber = reader.get[PostId]("postId")

      MentionedInThread(mentionedBy, topic, category, postNumber)
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
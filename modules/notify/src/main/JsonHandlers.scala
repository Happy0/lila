package lila.notify

import play.api.libs.json.{JsValue, Json, Writes}

object JSONHandlers {

  implicit val notificationWrites : Writes[Notification] = new Writes[Notification] {
    def writeBody(notificationContent: NotificationContent) = {
      notificationContent match {
        case MentionedInThread(mentionedBy, topic, category, postNumber) =>
          Json.obj("mentionedBy" -> mentionedBy.value, "topic" -> topic.value, "category" -> category.value, "postNumber" -> postNumber.value)
      }
    }

    def writes(notification: Notification) = {
        val body = notification.content

        val notificationType = body match {
          case MentionedInThread(_, _, _, _) => "mentioned"
        }

        Json.obj("content" -> writeBody(body), "type" -> notificationType, "date" -> notification.createdAt)
    }
  }

  implicit val newNotificationWrites: Writes[NewNotification] = new Writes[NewNotification] {

    def writes(newNotification: NewNotification) = {
      Json.obj("notification" -> newNotification.notification, "unread" -> newNotification.unreadNotifications)
    }
  }
}



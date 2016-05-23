package lila.notify

import play.api.libs.json.{JsValue, Json, Writes}

private object JSONHandlers {
  implicit val notificationWrites: Writes[NewNotification] = new Writes[NewNotification] {

    def writeBody(notificationContent: NotificationContent) = {
      notificationContent match {
        case MentionedInThread(mentionedBy, topic, category) =>
          Json.obj("mentionedBy" -> mentionedBy.value, "topic" -> topic.value, "category" -> category.value)
      }
    }

    def writeNotification(notification: Notification): JsValue = {
      val body = notification.content

      val notificationType = body match {
        case MentionedInThread(_, _, _) => "mentioned"
      }

      Json.obj("content" -> writeBody(body), "type" -> notificationType, "date" -> notification.createdAt)
    }

    def writes(newNotification: NewNotification) = {
      Json.obj("notification" -> writeNotification(newNotification.notification), "unread" -> newNotification.unreadNotifications)
    }

  }
}



package lila.notify

import org.joda.time.DateTime

sealed trait NotificationContent

case class MentionedInThread(mentionedBy: String, topicId: String, postId: String) extends NotificationContent

case class Notification(content: NotificationContent, createdAt: DateTime)
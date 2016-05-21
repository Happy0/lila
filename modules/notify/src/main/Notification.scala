package lila.notify

sealed trait Notification

case class MentionedInThread(mentionedBy: String, topicId: String, postId: String) extends Notification
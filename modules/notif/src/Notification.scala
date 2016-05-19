package lila.notif

import lila.forum.{Topic, Post}

sealed trait Notification

case class MentionedInThread(mentionedBy: String, topicId: String, postId: String) extends Notification
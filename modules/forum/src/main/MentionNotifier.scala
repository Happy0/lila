package lila.forum

import lila.common.Future
import lila.hub.actorApi.notify.{Notification, MentionedInThread}
import lila.notify.NotifyApi
import org.joda.time.DateTime

/**
  * Notifier to inform users if they have been mentioned in a post
  *
  * @param messageApi Api for sending inbox messages
  */
final class MentionNotifier(notifyApi: NotifyApi) {

  /**
    * Notify any users mentioned in a post that they have been mentioned
    *
    * @param post The post which may or may not mention users
    * @return
    */
  def notifyMentionedUsers(post: Post, topic: Topic): Unit = {

    post.userId match {
      case None => fuccess()
      case Some(author) =>
        val mentionedUsers = extractMentionedUsers(post)
        mentionedUsers.foreach(informOfMention(post, topic, _, author))
    }
  }

  /**
    * Inform user that they have been mentioned by another user
    *
    * @param post          The post that mentions the user
    * @param topic         The topic of the post that mentions the user
    * @param mentionedUser The user that was mentioned
    * @param mentionedBy   The user that mentioned the user
    * @return
    */
  def informOfMention(post: Post, topic: Topic, mentionedUser: String, mentionedBy: String): Unit = {
    val notification = Notification(mentionedUser, MentionedInThread(mentionedBy, post.categId, topic.name), false, DateTime.now)

    notifyApi.addNotification(notification)
  }

  /**
    * Pull out any users mentioned in a post
    *
    * @param post The post which may or may not mention users
    * @return
    */
  def extractMentionedUsers(post: Post): List[String] = {
    val postText = post.text

    //TODO: Extract using same regex used to highlight usernames
    if (postText.contains('@')) {
      val postWords = postText.split(' ')
      postWords.filter(_.startsWith("@")).distinct.map(_.tail).toList
    } else List()
  }
}
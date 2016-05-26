package lila.forum

import lila.common.Future
import lila.notify.{Notification, MentionedInThread}
import lila.notify.NotifyApi
import org.joda.time.DateTime

/**
  * Notifier to inform users if they have been mentioned in a post
  *
  * @param notifyApi Api for sending inbox messages
  */
final class MentionNotifier(notifyApi: NotifyApi) {

  /**
    * Notify any users mentioned in a post that they have been mentioned
    *
    * @param post The post which may or may not mention users
    * @return
    */
  def notifyMentionedUsers(post: Post, topic: Topic): Unit = {
    post.userId foreach { author =>
      val mentionedUsers = extractMentionedUsers(post).map(Notification.Notifies)
      val mentionedBy = MentionedInThread.MentionedBy(author)

      mentionedUsers.foreach(informOfMention(post, topic, _, mentionedBy))
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
  def informOfMention(post: Post, topic: Topic, mentionedUser: Notification.Notifies, mentionedBy: MentionedInThread.MentionedBy): Unit = {
    val notificationContent = MentionedInThread(
        mentionedBy,
        MentionedInThread.Topic(topic.name),
        MentionedInThread.Category(post.categId),
        MentionedInThread.PostNumber(post.number))

    val notification = Notification(mentionedUser, notificationContent, Notification.NotificationRead(false), DateTime.now)

    notifyApi.addNotification(notification)
  }

  /**
    * Pull out any users mentioned in a post
    *
    * @param post The post which may or may not mention users
    * @return
    */
  private def extractMentionedUsers(post: Post): Set[String] = {
    val postText = post.text

    //TODO: Extract using same regex used to highlight usernames
    if (postText.contains('@')) {
      val postWords = postText.split(' ')
      postWords.filter(_.startsWith("@")).map(_.tail).toSet
    } else Set()
  }
}
package lila.forum

import lila.common.Future
import lila.notify.{Notification, MentionedInThread}
import lila.notify.NotifyApi
import lila.user.{UserRepo, User}
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
      val mentionedUsers = extractMentionedUsers(post)
      val mentionedBy = MentionedInThread.MentionedBy(author)

      for {
        validUsers <- filterValidUsers(mentionedUsers)
        notifications = validUsers.map(mentioned => createMentionNotification(post, topic, mentioned, mentionedBy))
      } yield notifyApi.addNotifications(notifications)
    }
  }

  /**
    * Checks the database for valid users, and removes any users that are not valid
    * @param users
    * @return
    */
  private def filterValidUsers(users: Set[String]) : Fu[List[Notification.Notifies]] = {
    for {
      validUsers <- UserRepo.byIds(users)
      validNotifies = validUsers.map(user => Notification.Notifies(user.username))
    } yield validNotifies.pp
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
  def createMentionNotification(post: Post, topic: Topic, mentionedUser: Notification.Notifies, mentionedBy: MentionedInThread.MentionedBy): Notification = {
    val notificationContent = MentionedInThread(
        mentionedBy,
        MentionedInThread.Topic(topic.name),
        MentionedInThread.Category(post.categId),
        MentionedInThread.PostId(post.id))

    Notification(mentionedUser, notificationContent, Notification.NotificationRead(false), DateTime.now)
  }

  /**
    * Pull out any users mentioned in a post
    *
    * @param post The post which may or may not mention users
    * @return
    */
  private def extractMentionedUsers(post: Post): Set[String] = {
    User.atUsernameRegex.findAllMatchIn(post.text).map(_.matched.tail).toSet
  }
}
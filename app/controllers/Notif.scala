package controllers

import lila.app._
import lila.notify.Notification.Notifies

import play.api.libs.json._

object Notif extends LilaController {

  import lila.notify.JSONHandlers._

  val env = Env.notif

  def recent = Auth { implicit ctx =>
    me =>
      val notifies = Notifies(me.id)
      env.notifyApi.getNotifications(notifies, 1, 10) map {
        all => Ok(Json.toJson(all.currentPageResults)) as JSON
      }
  }
}

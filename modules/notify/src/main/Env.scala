package lila.notify

import akka.actor.ActorSystem

final class Env(db: lila.db.Env, system: ActorSystem) {

  lazy val notifyApi = new NotifyApi(bus = system.lilaBus)


}

object Env {

  lazy val current = "notify" boot new Env(db = lila.db.Env.current, system = lila.common.PlayApp.system)

}
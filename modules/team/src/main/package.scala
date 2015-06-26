package lila

import lila.socket.WithSocket

package object team extends PackageObject with WithPlay with WithSocket {

  object tube {

    implicit lazy val teamTube = Team.tube inColl Env.current.teamColl

    private[team] implicit lazy val requestTube = Request.tube inColl Env.current.requestColl

    private[team] implicit lazy val memberTube = Member.tube inColl Env.current.memberColl
  }

}

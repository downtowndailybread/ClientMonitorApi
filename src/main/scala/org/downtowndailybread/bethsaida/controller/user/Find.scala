package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Find extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with SettingsProvider =>

  val user_findRoute = path(JavaUUID) {
    uid =>
      authorizeNotAnonymous {
        implicit authUser =>
          get {
            futureComplete(runSql(c =>
              new UserRequest(settings, c).getRawUserFromUuid(uid)))
          }
      }
  }
}
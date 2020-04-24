package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.UserRequest

trait ForgotPassword extends ControllerBase {
  this: AuthenticationProvider
    with JsonSupport
    with DatabaseConnectionProvider
    with SettingsProvider =>

  val user_forgotPasswordRoute = path("forgot-password" / Segment) {
    email =>
      post {
        futureComplete(runSql {
          c =>
            new UserRequest(settings, c).initiatePasswordReset(email)
            "complete"
        })
      }
  }
}

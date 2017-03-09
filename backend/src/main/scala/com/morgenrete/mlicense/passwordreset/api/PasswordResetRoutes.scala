package com.morgenrete.mlicense.passwordreset.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.JsonSupport
import com.morgenrete.mlicense.passwordreset.application.PasswordResetService
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._

trait PasswordResetRoutes extends JsonSupport with StrictLogging {

  def passwordResetService: PasswordResetService

  val passwordResetRoutes = pathPrefix("passwordreset") {
    post {
      path(Segment) { code =>
        entity(as[PasswordResetInput]) { in =>
          onSuccess(passwordResetService.performPasswordReset(code, in.password)) {
            case Left(e) => complete(StatusCodes.Forbidden, e)
            case _ => complete("Ok")
          }
        }
      } ~ entity(as[ForgotPasswordInput]) { in =>
        onSuccess(passwordResetService.sendResetCodeToUser(in.login)) {
          complete("success")
        }
      }
    }
  }
}

case class PasswordResetInput(password: String)

case class ForgotPasswordInput(login: String)

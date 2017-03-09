package com.morgenrete.mlicense.user.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.Utils
import com.morgenrete.mlicense.common.api.JsonSupport
import com.morgenrete.mlicense.user.UserId
import com.morgenrete.mlicense.user.application.{UserRegisterResult, UserService}
import com.morgenrete.mlicense.user.domain.BasicUserData
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._

import scala.concurrent.Future

trait UsersRoutes extends JsonSupport with StrictLogging {

  def userService: UserService

  private implicit val userCbs = CanBeSerialized[BasicUserData]

  val usersRoutes = pathPrefix("users") {
    path("logout") {
      get {
        complete("ok")
      }
    } ~
      path("register") {
        post {
          entity(as[RegistrationInput]) { in =>
            onSuccess(userService.registerNewUser(in.loginEscaped, in.email, in.password)) {
              case UserRegisterResult.InvalidData(msg) => complete(StatusCodes.BadRequest, msg)
              case UserRegisterResult.UserExists(msg) => complete(StatusCodes.Conflict, msg)
              case UserRegisterResult.Success => complete("success")
            }
          }
        }
      } ~
      path("changepassword") {
        post {
          entity(as[ChangePasswordInput]) { in =>
            onSuccess(userService.changePassword(in.userId, in.currentPassword, in.newPassword)) {
              case Left(msg) => complete(StatusCodes.Forbidden, msg)
              case Right(_) => complete("Ok")
            }
          }
        }
      } ~
      pathEnd {
        post {
          entity(as[LoginInput]) { in =>
            onSuccess(userService.authenticate(in.login, in.password)) {
              case None => reject(AuthorizationFailedRejection)
              case Some(user) => complete(user)
            }
          }
        } ~
          patch {
            entity(as[PatchUserInput]) { in =>
              val userId = in.userId
              val updateAction = (in.login, in.email) match {
                case (Some(login), _) => userService.changeLogin(userId, login)
                case (_, Some(email)) => userService.changeEmail(userId, email)
                case _ => Future.successful(Left("You have to provide new login or email"))
              }

              onSuccess(updateAction) {
                case Left(msg) => complete(StatusCodes.Conflict, msg)
                case Right(_) => complete("Ok")
              }
            }
          }
      }
  }
}

case class RegistrationInput(login: String, email: String, password: String) {
  def loginEscaped = Utils.escapeHtml(login)
}


case class ChangePasswordInput(userId: UserId,
                               currentPassword: String,
                               newPassword: String)

case class LoginInput(login: String, password: String, rememberMe: Option[Boolean])

case class PatchUserInput(userId: UserId, login: Option[String], email: Option[String])
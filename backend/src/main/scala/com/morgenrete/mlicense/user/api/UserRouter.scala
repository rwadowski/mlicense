package com.morgenrete.mlicense.user.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import com.typesafe.scalalogging.StrictLogging
import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.JsonSupport
import com.morgenrete.mlicense.user.application.{UserRegisterResult, UserService}
import com.morgenrete.mlicense.user.domain.{BasicUserData, LoginInput, RegistrationInput}
import io.circe.generic.auto._

/**
  * Created by rwadowski on 3/4/17.
  */
trait UserRouter extends JsonSupport with StrictLogging {

  def userService: UserService

  private implicit val userCbs = CanBeSerialized[BasicUserData]

  val userRoutes = pathPrefix("user") {
    path("logout") {
      complete("Logout")
    } ~
    path("register") {
      entity(as[RegistrationInput]) { in =>
        onSuccess(userService.registerNewUser(in)) {
          case UserRegisterResult.InvalidData(msg) => complete(StatusCodes.BadRequest, msg)
          case UserRegisterResult.UserExists(msg) => complete(StatusCodes.Conflict, msg)
          case UserRegisterResult.Success => complete("success")
        }
      }
    } ~
    pathEnd {
      post {
        entity(as[LoginInput]) { in =>
          onSuccess(userService.authenticate(in)) {
            case None => reject(AuthorizationFailedRejection)
            case Some(user) => complete(user)
          }
        }
      }
    }
  }
}

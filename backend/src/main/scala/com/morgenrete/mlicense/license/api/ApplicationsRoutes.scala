package com.morgenrete.mlicense.license.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.{JsonSupport, SessionSupport}
import com.morgenrete.mlicense.license.application.{ApplicationService, CreateApplicationResult, UpdateApplicationResult}
import com.morgenrete.mlicense.license.domain.{Application, CreateApplication, UpdateApplication}
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._

/**
  * Created by rwadowski on 28.03.17.
  */
trait ApplicationsRoutes extends JsonSupport with SessionSupport with StrictLogging {

  def applicationService: ApplicationService

  private implicit val applicationCbs = CanBeSerialized[Application]

  val applicationsRoutes = pathPrefix("applications") {
    userIdFromSession { userId =>
      path(JavaUUID) { appId =>
        pathEndOrSingleSlash {
          get {
            onSuccess(applicationService.findById(appId)) {
              case None => complete(StatusCodes.NotFound)
              case Some(app) => complete(app)
            }
          }
        }
      } ~ pathEndOrSingleSlash {
        post {
          entity(as[CreateApplication]) { in =>
            onSuccess(applicationService.create(in.toApplication(userId))) {
              case CreateApplicationResult.Success => complete(StatusCodes.OK)
              case CreateApplicationResult.ApplicationExists => complete(StatusCodes.Conflict, "application exists")
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        } ~ get {
          onSuccess(applicationService.allForUser(userId)) { apps =>
            complete(apps.toList)
          }
        } ~ patch {
          entity(as[UpdateApplication]) { in =>
            onSuccess(applicationService.update(in.toApplication(userId))) {
              case UpdateApplicationResult.Success => complete(StatusCodes.OK)
              case UpdateApplicationResult.ApplicationNotExists => complete(StatusCodes.Conflict, "there is no such application")
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }

  }
}

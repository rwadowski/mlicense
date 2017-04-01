package com.morgenrete.mlicense.license.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.{JsonSupport, SessionSupport}
import com.morgenrete.mlicense.license.application.{CreateLicenseResult, LicenseService, UpdateLicenseResult}
import com.morgenrete.mlicense.license.domain.{CreateLicense, License, UpdateLicense}
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._

/**
  * Created by rwadowski on 28.03.17.
  */
trait LicensesRoutes extends JsonSupport with SessionSupport with StrictLogging {

  def licenseService: LicenseService

  implicit val licenseCbs = CanBeSerialized[License]

  val licensesRoutes = pathPrefix("licenses") {
    userIdFromSession { userId =>
      path(JavaUUID) { licenseId =>
        pathEndOrSingleSlash {
          get {
            onSuccess(licenseService.findById(licenseId)) {
              case None => complete(StatusCodes.NotFound)
              case Some(lic) => complete(lic)
            }
          }
        }
      } ~ pathEndOrSingleSlash {
        get {
          onSuccess(licenseService.allForUser(userId)) { licenses =>
            complete(licenses.toList)
          }
        } ~ post {
          entity(as[CreateLicense]) { in =>
            onSuccess(licenseService.create(in.toLicense(userId))) {
              case CreateLicenseResult.Success => complete(StatusCodes.OK)
              case CreateLicenseResult.LicenseExists => complete(StatusCodes.Conflict)
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        } ~ patch {
          entity(as[UpdateLicense]) { in =>
            onSuccess(licenseService.update(in.toLicense(userId))) {
              case UpdateLicenseResult.Success => complete(StatusCodes.OK)
              case UpdateLicenseResult.LicenseNotExists => complete(StatusCodes.Conflict)
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  }
}

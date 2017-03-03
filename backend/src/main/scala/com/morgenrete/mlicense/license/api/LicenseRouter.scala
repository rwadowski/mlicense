package com.morgenrete.mlicense.license.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging

/**
  * Created by rwadowski on 3/2/17.
  */
trait LicenseRouter extends StrictLogging {

  val licenseRoutes = pathPrefix("license") {
    pathPrefix(Segment) { appId =>
      pathEndOrSingleSlash {
        get {
          complete("OK got it")
        }
      }
    }
  }
}

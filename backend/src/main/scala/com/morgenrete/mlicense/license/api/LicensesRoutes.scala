package com.morgenrete.mlicense.license.api

import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.license.application.LicenseService
import com.typesafe.scalalogging.StrictLogging

trait LicensesRoutes extends StrictLogging {

  def licenseService: LicenseService

  val licenseRoutes = pathPrefix("license") {
    pathPrefix(Segment) { licenseId =>
      pathEndOrSingleSlash {
        get {
          complete("OK got it")
        }
      }
    }
  }
}

package com.morgenrete.mlicense.license.api

import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging

trait LicenseRoutes extends StrictLogging {

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

package com.morgenrete.mlicense

import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.license.api.LicenseRouter
import com.morgenrete.mlicense.util.RoutesRequestWrapper

/**
  * Created by rwadowski on 3/2/17.
  */
trait Routes extends LicenseRouter with RoutesRequestWrapper {

  val routes = requestWrapper {
    pathPrefix("mlicense") {
      licenseRoutes
    }
  }
}

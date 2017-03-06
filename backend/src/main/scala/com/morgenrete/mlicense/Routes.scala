package com.morgenrete.mlicense

import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.RoutesRequestWrapper
import com.morgenrete.mlicense.license.api.LicenseRouter
import com.morgenrete.mlicense.user.api.UserRouter

/**
  * Created by rwadowski on 3/2/17.
  */
trait Routes extends LicenseRouter with UserRouter with RoutesRequestWrapper {

  val routes = requestWrapper {
    pathPrefix("mlicense") {
      licenseRoutes ~
      userRoutes
    }
  }
}

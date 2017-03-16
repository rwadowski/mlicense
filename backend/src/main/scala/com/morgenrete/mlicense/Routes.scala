package com.morgenrete.mlicense

import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.RoutesRequestWrapper
import com.morgenrete.mlicense.license.api.LicenseRoutes
import com.morgenrete.mlicense.user.api.UsersRoutes

/**
  * Created by rwadowski on 3/2/17.
  */
trait Routes extends LicenseRoutes with UsersRoutes with RoutesRequestWrapper {

  val routes = requestWrapper {
    pathPrefix("mlicense") {
      licenseRoutes ~
        usersRoutes
    } ~ getFromResourceDirectory("prod") ~
      path("") {
        getFromResource("prod/index.html")
      }
  }
}

package com.morgenrete.mlicense

import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.RoutesRequestWrapper
import com.morgenrete.mlicense.license.api.{ApplicationsRoutes, CustomersRoutes, LicensesRoutes}
import com.morgenrete.mlicense.passwordreset.api.PasswordResetRoutes
import com.morgenrete.mlicense.user.api.UsersRoutes

/**
  * Created by rwadowski on 3/2/17.
  */
trait Routes extends LicensesRoutes
  with UsersRoutes
  with ApplicationsRoutes
  with CustomersRoutes
  with PasswordResetRoutes
  with RoutesRequestWrapper {

  val routes = requestWrapper {
    pathPrefix("mlicense") {
      licensesRoutes ~
        usersRoutes ~
        applicationsRoutes ~
        customersRoutes ~
        passwordResetRoutes
    } ~ getFromResourceDirectory("prod") ~
      path("") {
        getFromResource("prod/index.html")
      }
  }
}

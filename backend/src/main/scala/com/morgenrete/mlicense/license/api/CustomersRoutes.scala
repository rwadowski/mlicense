package com.morgenrete.mlicense.license.api

import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.license.application.CustomerService
import com.typesafe.scalalogging.StrictLogging
/**
  * Created by rwadowski on 28.03.17.
  */
trait CustomersRoutes extends StrictLogging {

  def customerService: CustomerService

  val customersRoutes = pathPrefix("customer") {
    pathEndOrSingleSlash {
      get {
        complete("OK got it")
      }
    }
  }
}

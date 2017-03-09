package com.morgenrete.mlicense.test

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.morgenrete.mlicense.common.api.JsonSupport
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers

import scala.concurrent.duration._

trait BaseRoutesSpec extends FlatSpecWithDb with ScalatestRouteTest with Matchers with JsonSupport { spec =>

  implicit def mapCbs = CanBeSerialized[Map[String, String]]

  implicit val timeout = RouteTestTimeout(10 second span)

  trait TestRoutesSupport {
    implicit def materializer = spec.materializer
    implicit def ec = spec.executor
    implicit def refreshTokenStorage = null
  }
}

package com.morgenrete.mlicense.test

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.stream.Materializer
import com.morgenrete.mlicense.common.api.JsonSupport
import com.morgenrete.mlicense.user.application.Session
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import io.circe._
import io.circe.syntax._

trait BaseRoutesSpec extends FlatSpecWithDb with ScalatestRouteTest with Matchers with JsonSupport { spec =>

  lazy val sessionConfig: SessionConfig = SessionConfig.fromConfig(ConfigFactory.load()).copy(sessionEncryptData = true)

  implicit def mapStringCbs = CanBeSerialized[Map[String, String]]
  implicit def mapBooleanOrStringCbs = CanBeSerialized[Map[String, Either[Boolean, String]]]
  implicit def booleanOrStringEncoder: Encoder[Either[Boolean, String]] = Encoder.instance(_.fold(_.asJson, _.asJson))
  implicit def booleanOrStringDecoder: Decoder[Either[Boolean, String]] = Decoder[Boolean].map{Left(_)}.or(Decoder[String].map{Right(_)})

  implicit val timeout = RouteTestTimeout(10 second span)

  trait TestRoutesSupport {
    lazy val sessionConfig = spec.sessionConfig
    implicit def materializer: Materializer = spec.materializer
    implicit def ec: ExecutionContext = spec.executor
    implicit def sessionManager = new SessionManager[Session](sessionConfig)
    implicit def refreshTokenStorage = null
  }
}

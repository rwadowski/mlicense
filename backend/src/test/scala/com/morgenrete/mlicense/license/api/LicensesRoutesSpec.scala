package com.morgenrete.mlicense.license.api

import java.time.format.DateTimeFormatter
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.morgenrete.mlicense.license.application.LicenseService
import com.morgenrete.mlicense.test.{BaseRoutesSpec, TestHelpersWithDb}
import com.morgenrete.mlicense.user.api.UsersRoutes
import com.morgenrete.mlicense.user.application.UserService

/**
  * Created by rwadowski on 31.03.17.
  */
class LicensesRoutesSpec extends BaseRoutesSpec with TestHelpersWithDb {
  spec =>

  val server = new LicensesRoutes with UsersRoutes with TestRoutesSupport {
    override val licenseService: LicenseService = spec.licenseService
    override val userService: UserService = spec.userService
  }

  val routes = Route.seal(
    server.licensesRoutes ~
    server.usersRoutes
  )

  def withLoggedInUser(login: String, password: String)(body: RequestTransformer => Unit) = {
    Post("/users", Map("login" -> login, "password" -> password)) ~> routes ~> check {
      status should be (StatusCodes.OK)

      val Some(sessionCookie) = header[`Set-Cookie`]

      body(addHeader(Cookie(sessionConfig.sessionCookieConfig.name, sessionCookie.cookie.value)))
    }
  }

  "POST /licenses" should "create new license" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val app1 = newRandomStoredApplication(user.id)
    val cus1 = newRandomStoredCustomer(user.id)
    val name = "name"
    withLoggedInUser("user1", "pass") { transform =>
      val json = Map[String, Either[Boolean, String]](
        "applicationId" -> Right(app1.id.toString),
        "customerId" -> Right(cus1.id.toString),
        "active" -> Left(true),
        "expirationDate" -> Right(validExpirationDate(2).toString),
        "name" -> Right(name)
      )
      Post("/licenses", json) ~> transform ~> routes ~> check {
        licenseDao.findByName(name).futureValue should be ('defined)
        status should be (StatusCodes.OK)
      }
    }
  }

  "GET /licenses" should "fetch all licenses for user" in {
    val user1 = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user1).futureValue
    val user2 = newUser("user2", "user2@sml.com", "pass2", "salt2")
    userDao.add(user2).futureValue
    val app1 = newRandomStoredApplication(user1.id)
    val app2 = newRandomStoredApplication(user2.id)
    val cus1 = newRandomStoredCustomer(user1.id)
    val cus2 = newRandomStoredCustomer(user1.id)
    val cus3 = newRandomStoredCustomer(user2.id)
    val lic1 = newStoredLicense(user1.id, app1.id, cus1.id)
    val lic2 = newStoredLicense(user1.id, app1.id, cus2.id)
    val lic3 = newStoredLicense(user2.id, app1.id, cus1.id)
    withLoggedInUser("user1", "pass") { transform =>
      Get("/licenses") ~> transform ~> routes ~> check {
        val expected =
          s"""[{"id":"${lic1.id.toString}","userId":"${user1.id}","applicationId":"${lic1.applicationId.toString}","customerId":"${lic1.customerId.toString}","active":true,"expirationDate":"${lic1.expirationDate.toString}","name":"${lic1.name}"},
              |{"id":"${lic2.id.toString}","userId":"${user1.id}","applicationId":"${lic2.applicationId.toString}","customerId":"${lic2.customerId.toString}","active":true,"expirationDate":"${lic2.expirationDate.toString}","name":"${lic2.name}"}]""".stripMargin.replace("\n", "")
        responseAs[String] shouldEqual expected
        status should be (StatusCodes.OK)
      }
    }
  }

  "PATCH /licenses" should "update license that exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val newName = "lic_new_name"
    val lic = newRandomStoredLicense(Some(user))
    withLoggedInUser("user1", "pass") { transform =>
      val json = Map[String, Either[Boolean, String]](
        "id" -> Right(lic.id.toString),
        "userId" -> Right(user.id.toString),
        "applicationId" -> Right(lic.applicationId.toString),
        "customerId" -> Right(lic.customerId.toString),
        "active" -> Left(lic.active),
        "expirationDate" -> Right(lic.expirationDate.toString),
        "name" -> Right(newName)
      )
      Patch("/licenses", json) ~> transform ~> routes ~> check {
        val res = licenseDao.findById(lic.id).futureValue
        val expected = lic.copy(name = newName)
        res should be ('defined)
        res shouldEqual Some(expected)
      }
    }
  }

  "PATCH /licenses" should "not update license that not exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val newName = "cus_name"
    val cus = newRandomCustomer(user.id)
    val app = newRandomApplication(user.id)
    val lic = newRandomLicense(user.id, app.id, cus.id)
    withLoggedInUser("user1", "pass") { transform =>
      val json = Map[String, Either[Boolean, String]](
        "id" -> Right(lic.id.toString),
        "userId" -> Right(user.id.toString),
        "applicationId" -> Right(lic.applicationId.toString),
        "customerId" -> Right(lic.customerId.toString),
        "active" -> Left(lic.active),
        "expirationDate" -> Right(lic.expirationDate.toString),
        "name" -> Right(newName)
      )
      Patch("/licenses", json) ~> transform ~> routes ~> check {
        status shouldEqual (StatusCodes.Conflict)
      }
    }
  }

  "GET /licenses/:id" should "fetch particular license if exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val lic = newRandomStoredLicense(Some(user))
    withLoggedInUser("user1", "pass") { transform =>
      val path = s"/licenses/${lic.id.toString}"
      Get(path) ~> transform ~> routes ~> check {
        val expirationDate = DateTimeFormatter.ISO_INSTANT.format(lic.expirationDate)
        val expected = s"""{"id":"${lic.id.toString}","userId":"${lic.userId.toString}","applicationId":"${lic.applicationId.toString}","customerId":"${lic.customerId.toString}","active":true,"expirationDate":"$expirationDate","name":"${lic.name}"}"""
        responseAs[String] shouldEqual expected
      }
    }
  }

  "GET /licenses/:id" should "not fetch particular license if not exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val licId = UUID.randomUUID().toString
    withLoggedInUser("user1", "pass") { transform =>
      val path = s"/licenses/$licId"
      Get(path) ~> transform ~> routes ~> check {
        status shouldEqual (StatusCodes.NotFound)
      }
    }
  }
}

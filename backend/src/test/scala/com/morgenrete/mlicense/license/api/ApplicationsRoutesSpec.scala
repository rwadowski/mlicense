package com.morgenrete.mlicense.license.api

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.morgenrete.mlicense.license.application.ApplicationService
import com.morgenrete.mlicense.test.{BaseRoutesSpec, TestHelpersWithDb}
import com.morgenrete.mlicense.user.api.UsersRoutes
import com.morgenrete.mlicense.user.application.UserService

/**
  * Created by rwadowski on 29.03.17.
  */
class ApplicationsRoutesSpec extends BaseRoutesSpec with TestHelpersWithDb {
  spec =>

  val server = new ApplicationsRoutes with UsersRoutes with TestRoutesSupport {
    override val applicationService: ApplicationService = spec.applicationService
    override val userService: UserService = spec.userService
  }

  val routes = Route.seal(
    server.applicationsRoutes ~
    server.usersRoutes
  )

  def withLoggedInUser(login: String, password: String)(body: RequestTransformer => Unit) = {
    Post("/users", Map("login" -> login, "password" -> password)) ~> routes ~> check {
      status should be (StatusCodes.OK)

      val Some(sessionCookie) = header[`Set-Cookie`]

      body(addHeader(Cookie(sessionConfig.sessionCookieConfig.name, sessionCookie.cookie.value)))
    }
  }

  "POST /applications" should "create new application if there is no application with given name" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val appName = "MyApp"
    withLoggedInUser("user1", "pass") { transform =>
      Post("/applications", Map("name" -> appName)) ~> transform ~> routes ~> check {
        applicationDao.findByName(appName).futureValue should be ('defined)
        status should be (StatusCodes.OK)
      }
    }
  }

  "POST /applications" should "not create new application if there is an app" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val app = newRandomStoredApplication(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      Post("/applications", Map("name" -> app.name)) ~> transform ~> routes ~> check {
        status should be (StatusCodes.Conflict)
      }
    }
  }

  "GET /applications" should "fetch all customers for user" in {
    val user1 = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user1).futureValue
    val user2 = newUser("user2", "user2@sml.com", "pass2", "salt2")
    userDao.add(user2).futureValue
    val app1 = newRandomStoredApplication(user1.id)
    val app2 = newRandomStoredApplication(user1.id)
    val app3 = newRandomStoredApplication(user2.id)
    withLoggedInUser("user1", "pass") { transform =>
      Get("/applications") ~> transform ~> routes ~> check {
        val expected = s"""[{"id":"${app1.id.toString}","name":"${app1.name}","userId":"${app1.userId.toString}"},{"id":"${app2.id.toString}","name":"${app2.name}","userId":"${app2.userId.toString}"}]"""
        responseAs[String] shouldEqual expected
        status should be (StatusCodes.OK)
      }
    }
  }

  "PATCH /applications" should "update application that exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val newName = "app_name"
    val app = newRandomStoredApplication(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      Patch("/applications", Map("id" -> app.id.toString, "name" -> newName, "userId" -> user.id.toString)) ~> transform ~> routes ~> check {
        val res = applicationDao.findById(app.id).futureValue
        val expected = app.copy(name = newName)
        res should be ('defined)
        res shouldEqual Some(expected)
      }
    }
  }

  "PATCH /applications" should "not update application that not exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val newName = "app_name"
    val app = newRandomApplication(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      Patch("/applications", Map("id" -> app.id.toString, "name" -> newName, "userId" -> user.id.toString)) ~> transform ~> routes ~> check {
        status shouldEqual (StatusCodes.Conflict)
      }
    }
  }

  "GET /applications/:id" should "fetch particular application if exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val app1 = newRandomStoredApplication(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      val path = s"/applications/${app1.id.toString}"
      Get(path) ~> transform ~> routes ~> check {
        val expected = s"""{"id":"${app1.id.toString}","name":"${app1.name}","userId":"${app1.userId.toString}"}"""
        responseAs[String] shouldEqual expected
      }
    }
  }

  "GET /applications/:id" should "not fetch particular application if not exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val appId = UUID.randomUUID().toString
    withLoggedInUser("user1", "pass") { transform =>
      val path = s"/applications/$appId"
      Get(path) ~> transform ~> routes ~> check {
        status shouldEqual (StatusCodes.NotFound)
      }
    }
  }
}

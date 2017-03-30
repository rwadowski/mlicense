package com.morgenrete.mlicense.license.api

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

  "POST /applications" should "create new application" in {
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

  "GET /applications" should "fetch all applications for user" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val app1 = newRandomStoredApplication(user.id)
    val app2 = newRandomStoredApplication(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      Get("/applications") ~> transform ~> routes ~> check {
        val expected = s"""[{"id":"${app1.id.toString}","name":"${app1.name}","userId":"${app1.userId.toString}"},{"id":"${app2.id.toString}","name":"${app2.name}","userId":"${app2.userId.toString}"}]"""
        responseAs[String] shouldEqual expected
        status should be (StatusCodes.OK)
      }
    }
  }

  "PATCH /applications" should "update application" in {
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

  "GET /applications/:id" should "fetch particular application" in {
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
}

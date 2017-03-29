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
}

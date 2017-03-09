package com.morgenrete.mlicense.user.api

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.morgenrete.mlicense.test.{BaseRoutesSpec, TestHelpersWithDb}

class UsersRoutesSpec extends BaseRoutesSpec with TestHelpersWithDb { spec =>

  val routes = Route.seal(new UsersRoutes with TestRoutesSupport {
    override val userService = spec.userService
  }.usersRoutes)

  val id = UUID.randomUUID().toString

  "POST /register" should "register new user" in {
    Post("/users/register", Map("id" -> id, "login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      userDao.findByLowerCasedLogin("newUser").futureValue should be ('defined)
      status should be (StatusCodes.OK)
    }
  }

  "POST /register with invalid data" should "result in an error" in {
    Post("/users/register") ~> routes ~> check {
      status should be (StatusCodes.BadRequest)
    }
  }

  "POST /users/whatever" should "not be bound to /users login - reject unmatchedPath request" in {
    Post("/users/whatever") ~> routes ~> check {
      status should be (StatusCodes.NotFound)
    }
  }

  "POST /register with an existing login" should "return 409 with an error message" in {
    userDao.add(newUser("user1", "user1@sml.com", "pass", "salt")).futureValue
    Post("/users/register", Map("login" -> "user1", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.Conflict)
      entityAs[String] should be ("Login already in use!")
    }
  }

  "POST /register with an existing email" should "return 409 with an error message" in {
    userDao.add(newUser("user2", "user2@sml.com", "pass", "salt")).futureValue
    Post("/users/register", Map("login" -> "newUser", "email" -> "user2@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.Conflict)
      entityAs[String] should be ("E-mail already in use!")
    }
  }

  "POST /register" should "use escaped Strings" in {
    Post("/users/register", Map("login" -> "<script>alert('haxor');</script>", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.OK)
      userDao.findByEmail("newUser@sml.com").futureValue.map(_.login) should be (Some("&lt;script&gt;alert('haxor');&lt;/script&gt;"))
    }
  }

  "POST /" should "not log in given invalid credentials" in {
    userDao.add(newUser("user4", "user4@sml.com", "pass", "salt")).futureValue
    Post("/users", Map("login" -> "user4", "password" -> "hacker")) ~> routes ~> check {
      status should be (StatusCodes.Forbidden)
    }
  }

  "PATCH /" should "update email when email is given" in {
    val user = newUser("user5", "user5@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val email = "coolmail@awesome.rox"

    Patch("/users", Map("userId" -> user.id.toString, "email" -> email)) ~> routes ~> check {
      userDao.findByLowerCasedLogin("user5").futureValue.map(_.email) should be(Some(email))
      status should be (StatusCodes.OK)
    }
  }

  "PATCH /" should "update login when login is given" in {
    val user = newUser("user6", "user6@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val login = "user6_changed"

    Patch("/users", Map("userId" -> user.id.toString, "login" -> login)) ~> routes ~> check {
      userDao.findByLowerCasedLogin(login).futureValue should be ('defined)
      status should be(StatusCodes.OK)
    }
  }

  "PATCH /" should "result in an error in neither email nor login is given" in {
    val user = newUser("user7", "user7@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    Patch("/users", Map("userId" -> user.id.toString)) ~> routes ~> check {
      status should be (StatusCodes.Conflict)
    }
  }

  "POST /changepassword" should "update password if current is correct and new is present" in {
    val user = newUser("user8", "user8@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    Post("/users/changepassword", Map("userId" -> user.id.toString, "currentPassword" -> "pass", "newPassword" -> "newPass")) ~> routes ~> check {
      status should be (StatusCodes.OK)
    }
  }

  "POST /changepassword" should "not update password if current is wrong" in {
    val user = newUser("user9", "user9@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    Post("/users/changepassword", Map("userId" -> user.id.toString ,"currentPassword" -> "hacker", "newPassword" -> "newPass")) ~> routes ~> check {
      status should be (StatusCodes.Forbidden)
    }
  }
}

package com.morgenrete.mlicense.license.api

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.morgenrete.mlicense.license.application.CustomerService
import com.morgenrete.mlicense.test.{BaseRoutesSpec, TestHelpersWithDb}
import com.morgenrete.mlicense.user.api.UsersRoutes
import com.morgenrete.mlicense.user.application.UserService
/**
  * Created by rwadowski on 31.03.17.
  */
class CustomerRoutesSpec extends BaseRoutesSpec with TestHelpersWithDb {
  spec =>

  val server = new CustomersRoutes with UsersRoutes with TestRoutesSupport {
    override val customerService: CustomerService = spec.customerService
    override val userService: UserService = spec.userService
  }

  val routes = Route.seal(
    server.customersRoutes ~
    server.usersRoutes
  )

  def withLoggedInUser(login: String, password: String)(body: RequestTransformer => Unit) = {
    Post("/users", Map("login" -> login, "password" -> password)) ~> routes ~> check {
      status should be (StatusCodes.OK)

      val Some(sessionCookie) = header[`Set-Cookie`]

      body(addHeader(Cookie(sessionConfig.sessionCookieConfig.name, sessionCookie.cookie.value)))
    }
  }

  "POST /customers" should "create new customer if there is no customer with given name" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val customerName = "MyApp"
    withLoggedInUser("user1", "pass") { transform =>
      Post("/customers", Map("name" -> customerName)) ~> transform ~> routes ~> check {
        customerDao.findByName(customerName).futureValue should be ('defined)
        status should be (StatusCodes.OK)
      }
    }
  }

  "POST /customers" should "not create new customer if there is customer with given name" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val customer = newRandomStoredCustomer(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      Post("/customers", Map("name" -> customer.name)) ~> transform ~> routes ~> check {
        status should be (StatusCodes.Conflict)
      }
    }
  }

  "GET /customers" should "fetch all customers for user" in {
    val user1 = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user1).futureValue
    val user2 = newUser("user2", "user2@sml.com", "pass2", "salt2")
    userDao.add(user2).futureValue
    val cus1 = newRandomStoredCustomer(user1.id)
    val cus2 = newRandomStoredCustomer(user1.id)
    val cus3 = newRandomStoredCustomer(user2.id)
    withLoggedInUser("user1", "pass") { transform =>
      Get("/customers") ~> transform ~> routes ~> check {
        val expected = s"""[{"id":"${cus1.id.toString}","name":"${cus1.name}","userId":"${cus1.userId.toString}"},{"id":"${cus2.id.toString}","name":"${cus2.name}","userId":"${cus2.userId.toString}"}]"""
        responseAs[String] shouldEqual expected
        status should be (StatusCodes.OK)
      }
    }
  }

  "PATCH /customers" should "update customer that exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val newName = "app_name"
    val cus = newRandomStoredCustomer(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      Patch("/customers", Map("id" -> cus.id.toString, "name" -> newName, "userId" -> user.id.toString)) ~> transform ~> routes ~> check {
        val res = customerDao.findById(cus.id).futureValue
        val expected = cus.copy(name = newName)
        res should be ('defined)
        res shouldEqual Some(expected)
      }
    }
  }

  "PATCH /customers" should "not update customer that not exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val newName = "cus_name"
    val cus = newRandomCustomer(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      Patch("/customers", Map("id" -> cus.id.toString, "name" -> newName, "userId" -> user.id.toString)) ~> transform ~> routes ~> check {
        status shouldEqual (StatusCodes.Conflict)
      }
    }
  }

  "GET /customers/:id" should "fetch particular customers if exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val cus1 = newRandomStoredCustomer(user.id)
    withLoggedInUser("user1", "pass") { transform =>
      val path = s"/customers/${cus1.id.toString}"
      Get(path) ~> transform ~> routes ~> check {
        val expected = s"""{"id":"${cus1.id.toString}","name":"${cus1.name}","userId":"${cus1.userId.toString}"}"""
        responseAs[String] shouldEqual expected
      }
    }
  }

  "GET /customers/:id" should "not fetch particular customer if not exists" in {
    val user = newUser("user1", "user1@sml.com", "pass", "salt")
    userDao.add(user).futureValue
    val cusId = UUID.randomUUID().toString
    withLoggedInUser("user1", "pass") { transform =>
      val path = s"/customers/$cusId"
      Get(path) ~> transform ~> routes ~> check {
        status shouldEqual (StatusCodes.NotFound)
      }
    }
  }
}

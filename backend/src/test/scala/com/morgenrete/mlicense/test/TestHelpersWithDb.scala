package com.morgenrete.mlicense.test

import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.email.application.{DummyEmailService, EmailTemplatingEngine}
import com.morgenrete.mlicense.license.application.{ApplicationDao, ApplicationService, CustomerDao, CustomerService}
import com.morgenrete.mlicense.license.domain.{Application, Customer}
import com.morgenrete.mlicense.user.application.{UserDao, UserService}
import com.morgenrete.mlicense.user.domain.User
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext

trait TestHelpersWithDb extends TestHelpers with ScalaFutures {

  lazy val emailService = new DummyEmailService
  lazy val emailTemplatingEngine = new EmailTemplatingEngine
  lazy val userDao = new UserDao(sqlDatabase)
  lazy val applicationDao = new ApplicationDao(sqlDatabase)
  lazy val customerDao = new CustomerDao(sqlDatabase)
  lazy val userService = new UserService(userDao, emailService, emailTemplatingEngine)

  lazy val applicationService = new ApplicationService(applicationDao)
  lazy val customerService = new CustomerService(customerDao)

  def sqlDatabase: SqlDatabase

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def newRandomStoredUser(password: Option[String] = None): User = {
    val u = newRandomUser(password)
    userDao.add(u).futureValue
    u
  }

  def newRandomStoredApplication: Application = {
    val application = newRandomApplication
    applicationDao.add(application).futureValue
    application
  }

  def newRandomStoredCustomer: Customer = {
    val customer = newRandomCustomer
    customerDao.add(customer).futureValue
    customer
  }
}

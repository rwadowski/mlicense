package com.morgenrete.mlicense.test

import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.email.application.{DummyEmailService, EmailTemplatingEngine}
import com.morgenrete.mlicense.license.{ApplicationId, CustomerId}
import com.morgenrete.mlicense.license.application._
import com.morgenrete.mlicense.license.domain.{Application, Customer, License}
import com.morgenrete.mlicense.user.UserId
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
  lazy val licenseDao = new LicenseDao(sqlDatabase)
  lazy val userService = new UserService(userDao, emailService, emailTemplatingEngine)

  lazy val applicationService = new ApplicationService(applicationDao)
  lazy val customerService = new CustomerService(customerDao)
  lazy val licenseService = new LicenseService(licenseDao)

  def sqlDatabase: SqlDatabase

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def newRandomStoredUser(password: Option[String] = None): User = {
    val u = newRandomUser(password)
    userDao.add(u).futureValue
    u
  }

  def newRandomStoredApplication(userId: UserId): Application = {
    val application = newRandomApplication(userId)
    applicationDao.add(application).futureValue
    application
  }

  def newRandomStoredCustomer(userId: UserId): Customer = {
    val customer = newRandomCustomer(userId)
    customerDao.add(customer).futureValue
    customer
  }

  def newRandomStoredLicense(user: Option[User] = None): License = {
    val u = user.getOrElse(newRandomStoredUser())
    val c = newRandomStoredCustomer(u.id)
    val a = newRandomStoredApplication(u.id)
    val l = newRandomLicense(u.id, a.id, c.id)
    licenseDao.add(l).futureValue
    l
  }

  def newStoredLicense(userId: UserId,
                       applicationId: ApplicationId,
                       customerId: CustomerId): License = {
    val l = newRandomLicense(userId, applicationId, customerId)
    licenseDao.add(l).futureValue
    l
  }
}

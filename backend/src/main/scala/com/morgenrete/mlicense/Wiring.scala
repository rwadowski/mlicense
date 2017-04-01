package com.morgenrete.mlicense

import akka.actor.ActorSystem
import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.config.MLicenseConfig
import com.morgenrete.mlicense.email.application.{EmailTemplatingEngine, SmtpEmailService}
import com.morgenrete.mlicense.license.application._
import com.morgenrete.mlicense.passwordreset.application.{PasswordResetCodeDao, PasswordResetService}
import com.morgenrete.mlicense.user.application.{RefreshTokenStorageImpl, RememberMeTokenDao, UserDao, UserService}

import scala.concurrent.ExecutionContext

/**
  * Created by rwadowski on 3/5/17.
  */
trait Wiring {

  def config: MLicenseConfig
  def system: ActorSystem

  lazy val daoEc: ExecutionContext = system.dispatchers.lookup("dao-dispatcher")
  lazy val serviceEc: ExecutionContext = system.dispatchers.lookup("service-dispatcher")

  lazy val sqlDatabase: SqlDatabase = SqlDatabase.create(config)

  lazy val userDao = new UserDao(sqlDatabase)(daoEc)
  lazy val codeDao = new PasswordResetCodeDao(sqlDatabase)(daoEc)
  lazy val applicationDao = new ApplicationDao(sqlDatabase)(daoEc)
  lazy val customerDao = new CustomerDao(sqlDatabase)(daoEc)
  lazy val licenseDao = new LicenseDao(sqlDatabase)(daoEc)
  lazy val rememberMeTokenDao = new RememberMeTokenDao(sqlDatabase)(daoEc)

  lazy val emailService = new SmtpEmailService(config)(serviceEc)
  lazy val emailTemplatingEngine = new EmailTemplatingEngine
  lazy val applicationService = new ApplicationService(applicationDao)(serviceEc)
  lazy val customerService = new CustomerService(customerDao)(serviceEc)
  lazy val licenseService = new LicenseService(licenseDao)(serviceEc)
  lazy val userService = new UserService(userDao, emailService, emailTemplatingEngine)(serviceEc)
  lazy val passwordResetService = new PasswordResetService(userDao, codeDao, emailService, emailTemplatingEngine, config)(serviceEc)
  lazy val refreshTokenStorage = new RefreshTokenStorageImpl(rememberMeTokenDao, system)(serviceEc)
}

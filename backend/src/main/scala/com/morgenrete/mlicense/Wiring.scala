package com.morgenrete.mlicense

import akka.actor.ActorSystem
import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.config.MLicenseConfig
import com.morgenrete.mlicense.user.application.{UserDao, UserService}

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

  lazy val userService = new UserService(userDao)(serviceEc)
}

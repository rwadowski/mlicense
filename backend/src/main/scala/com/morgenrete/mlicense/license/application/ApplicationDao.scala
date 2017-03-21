package com.morgenrete.mlicense.license.application

import java.util.UUID

import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.license.ApplicationId
import com.morgenrete.mlicense.license.domain.Application

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 20.03.17.
  */
class ApplicationDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext) extends SqlApplicationSchema {

  import database._
  import database.driver.api._
  import com.morgenrete.mlicense.common.FutureHelpers._

  def add(application: Application): Future[Unit] = db.run(applications += application).mapToUnit

  def findById(applicationId: ApplicationId): Future[Option[Application]] = db.run(applications.filter(_.id === applicationId).result.headOption)

  def findByName(name: String): Future[Option[Application]] = db.run(applications.filter(_.name === name).result.headOption)

  def delete(applicationId: ApplicationId): Future[Unit] = db.run(applications.filter(_.id === applicationId).delete).mapToUnit

  def update(application: Application): Future[Unit] = db.run(applications.filter(_.id === application.id).update(application)).mapToUnit
}

trait SqlApplicationSchema {

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val applications: TableQuery[Applications] = TableQuery[Applications]

  protected class Applications(tag: Tag) extends Table[Application](tag, "applications") {
    def id              = column[UUID]("id", O.PrimaryKey)
    def name            = column[String]("name")

    def * = (id, name) <> ((Application.apply _).tupled, Application.unapply)
  }
}

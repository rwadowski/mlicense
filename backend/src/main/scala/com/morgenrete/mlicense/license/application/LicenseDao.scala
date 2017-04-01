package com.morgenrete.mlicense.license.application

import java.time.OffsetDateTime
import java.util.{Date, UUID}

import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.license.LicenseId
import com.morgenrete.mlicense.license.domain.License
import com.morgenrete.mlicense.user.UserId
import com.morgenrete.mlicense.user.application.SqlUserSchema

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 20.03.17.
  */
class LicenseDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext)
  extends SqlLicenseSchema with SqlCustomerSchema with SqlUserSchema with SqlApplicationSchema {

  import com.morgenrete.mlicense.common.FutureHelpers._
  import database._
  import database.driver.api._

  def add(license: License): Future[Unit] = db.run(licenses += license).mapToUnit

  def findById(licenseId: LicenseId): Future[Option[License]] = db.run(licenses.filter(_.id === licenseId).result.headOption)

  def findByName(name: String): Future[Option[License]] = db.run(licenses.filter(_.name === name).result.headOption)

  def delete(licenseId: LicenseId): Future[Unit] = db.run(licenses.filter(_.id === licenseId).delete).mapToUnit

  def update(license: License): Future[Option[License]] = db.run(licenses.filter(l => l.id === license.id && l.userId === license.userId).update(license)).map{
    case 0 => None
    case _ => Some(license)
  }

  def allForUser(userId: UserId): Future[Seq[License]] = db.run(licenses.filter(_.userId === userId).result)
}

trait SqlLicenseSchema {
  this: SqlUserSchema with SqlCustomerSchema with SqlApplicationSchema =>

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val licenses: TableQuery[Licenses] = TableQuery[Licenses]

  protected class Licenses(tag: Tag) extends Table[License](tag, "licenses") {

    def id                = column[UUID]("id", O.PrimaryKey)
    def userId            = column[UUID]("user_id")
    def applicationId     = column[UUID]("application_id")
    def customerId        = column[UUID]("customer_id")
    def active            = column[Boolean]("active")
    def expirationDate    = column[OffsetDateTime]("expiration_date")
    def name              = column[String]("name")

    def user              = foreignKey("license_user_id_fk", userId, users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def application       = foreignKey("license_application_id_fk", applicationId, applications)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def customer          = foreignKey("license_customer_id_fk", customerId, customers)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, userId, applicationId, customerId, active, expirationDate, name) <> ((License.apply _).tupled, License.unapply)
  }
}
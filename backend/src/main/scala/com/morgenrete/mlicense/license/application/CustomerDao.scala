package com.morgenrete.mlicense.license.application

import java.util.UUID

import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.license.CustomerId
import com.morgenrete.mlicense.license.domain.Customer
import com.morgenrete.mlicense.user.UserId
import com.morgenrete.mlicense.user.application.SqlUserSchema

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 20.03.17.
  */
class CustomerDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext) extends SqlCustomerSchema with SqlUserSchema {

  import database._
  import database.driver.api._
  import com.morgenrete.mlicense.common.FutureHelpers._

  def add(customer: Customer): Future[Unit] = db.run(customers += customer).mapToUnit

  def findById(customerId: CustomerId): Future[Option[Customer]] = db.run(customers.filter(_.id === customerId).result.headOption)

  def findByName(name: String): Future[Option[Customer]] = db.run(customers.filter(_.name === name).result.headOption)

  def delete(customerId: CustomerId): Future[Unit] = db.run(customers.filter(_.id === customerId).delete).mapToUnit

  def update(customer: Customer): Future[Option[Customer]] = db.run(customers.filter(c => c.id === customer.id && c.userId === customer.userId).update(customer)).map{
    case 0 => Some(customer)
    case _ => None
  }

  def allForUser(userId: UserId): Future[Seq[Customer]] = db.run(customers.filter(_.userId === userId).result)
}

trait SqlCustomerSchema {
  this: SqlUserSchema =>

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val customers: TableQuery[Customers] = TableQuery[Customers]

  protected class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def id          = column[UUID]("id", O.PrimaryKey)
    def name        = column[String]("name")
    def userId      = column[UUID]("user_id")

    def user        = foreignKey("customer_user_id_fk", userId, users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, name, userId) <> ((Customer.apply _).tupled, Customer.unapply)
  }

}
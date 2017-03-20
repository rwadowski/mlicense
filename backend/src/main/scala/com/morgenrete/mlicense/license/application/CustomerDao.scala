package com.morgenrete.mlicense.license.application

import java.util.UUID

import com.morgenrete.mlicense.common.sql.SqlDatabase
import com.morgenrete.mlicense.license.CustomerId
import com.morgenrete.mlicense.license.domain.Customer

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 20.03.17.
  */
class CustomerDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext) extends SqlCustomerSchema {

  import database._
  import database.driver.api._
  import com.morgenrete.mlicense.common.FutureHelpers._

  def add(customer: Customer): Future[Unit] = db.run(customers += customer).mapToUnit

  def findById(customerId: CustomerId): Future[Option[Customer]] = db.run(customers.filter(_.id === customerId).result.headOption)

  def delete(customerId: CustomerId): Future[Unit] = db.run(customers.filter(_.id === customerId).delete).mapToUnit

  def update(customer: Customer): Future[Unit] = db.run(customers.filter(_.id === customer.id).update(customer)).mapToUnit
}

trait SqlCustomerSchema {

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val customers: TableQuery[Customers] = TableQuery[Customers]

  protected class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def id          = column[UUID]("id", O.PrimaryKey)
    def name        = column[String]("name")

    def * = (id, name) <> ((Customer.apply _).tupled, Customer.unapply)
  }

}
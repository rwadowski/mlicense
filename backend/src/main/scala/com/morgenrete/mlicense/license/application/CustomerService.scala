package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.license.CustomerId
import com.morgenrete.mlicense.license.domain.{CreateCustomer, Customer, UpdateCustomer}
import com.morgenrete.mlicense.user.UserId

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 27.03.17.
  */
class CustomerService(customerDao: CustomerDao)(implicit val ec: ExecutionContext) {

  def findById(customerId: CustomerId): Future[Option[Customer]] = {
    customerDao.findById(customerId)
  }

  def create(customer: Customer): Future[CreateCustomerResult] = {
    checkCustomerExistence(customer.name)(customerDao.findByName).flatMap {
      case Left(_) => Future.successful(CreateCustomerResult.CustomerExists)
      case Right(_) =>
        val res = customerDao.add(customer)
        res.map{_ => CreateCustomerResult.Success}
    }
  }

  private def checkCustomerExistence[T](criteria: T)(method: T => Future[Option[Customer]]): Future[Either[String, Unit]] = {
    method(criteria).map{
      case Some(_) => Left("Customer with this name already exists")
      case _ => Right((): Unit)
    }
  }

  def deleteById(customerId: CustomerId): Future[Unit] = {
    customerDao.delete(customerId)
  }

  def update(customer: Customer): Future[UpdateCustomerResult] = {
    checkCustomerExistence(customer.id)(customerDao.findById).flatMap{
      case Left(_) =>
        val result = customerDao.update(customer)
        result.map{_ => UpdateCustomerResult.Success}
      case Right(_) => Future.successful(UpdateCustomerResult.CustomerNotExists)
    }
  }

  def allForUser(userId: UserId): Future[Seq[Customer]] = {
    customerDao.allForUser(userId)
  }
}

sealed trait CreateCustomerResult

object CreateCustomerResult {

  case object Success extends CreateCustomerResult

  case object CustomerExists extends CreateCustomerResult

  case class InvalidData(msg: String) extends CreateCustomerResult
}

sealed trait UpdateCustomerResult

object UpdateCustomerResult {

  case object Success extends UpdateCustomerResult

  case object CustomerNotExists extends UpdateCustomerResult

  case class InvalidData(msg: String) extends UpdateCustomerResult
}
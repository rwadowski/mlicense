package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.license.domain.Customer
import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpers}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext

/**
  * Created by rwadowski on 21.03.17.
  */
class CustomerDaoSpec extends FlatSpecWithDb with StrictLogging with TestHelpers with Matchers {
  behavior of "CustomerDao"

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val customerDao = new CustomerDao(sqlDatabase)

  override def beforeEach(): Unit = {
    super.beforeEach()
    for(i <- 1 to randomIds.size) {
      val name = "customer_" + i
      customerDao.add(Customer(randomIds(i - 1), name)).futureValue
    }
  }

  it should "add new customer" in {
    //Given
    val name = "awesome_customer"
    val customer = newCustomer(name)

    //When
    customerDao.add(customer).futureValue

    //Then
    customerDao.findById(customer.id).futureValue should be ('defined)
  }

  it should "delete customer by id" in {
    //Given
    val name = "awesome_customer"
    val customer = newCustomer(name)

    //Then
    customerDao.add(customer).futureValue

    //Then
    customerDao.delete(customer.id).futureValue
    customerDao.findById(customer.id).futureValue should not be ('defined)
  }

  it should "update customer" in {
    //Given
    val name = "awesome_customer"
    val newName = "new_awesome_customer_name"
    val customer = newCustomer(name)
    val modifiedCustomer = Customer(customer.id, newName)

    //When
    customerDao.add(customer).futureValue
    customerDao.update(modifiedCustomer).futureValue

    //Then
    customerDao.findById(customer.id).futureValue should be ('defined)
    customerDao.findById(customer.id).futureValue shouldEqual Some(modifiedCustomer)
  }

  it should "find customer by name" in {
    //Given
    val name = "awesome_customer"
    val customer = newCustomer(name)

    //When
    customerDao.add(customer).futureValue

    //Then
    customerDao.findByName(customer.name).futureValue should be ('defined)
  }
}

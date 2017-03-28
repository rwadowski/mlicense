package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.license.domain.Customer
import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpersWithDb}
import com.morgenrete.mlicense.user.domain.User
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Matchers

/**
  * Created by rwadowski on 21.03.17.
  */
class CustomerDaoSpec extends FlatSpecWithDb with StrictLogging with Matchers with TestHelpersWithDb {
  behavior of "CustomerDao"

  val user: User = newRandomUser()

  override def beforeEach(): Unit = {
    super.beforeEach()
    userDao.add(user).futureValue
    for(i <- 1 to randomIds.size) {
      val name = "customer_" + i
      customerDao.add(Customer(randomIds(i - 1), name, user.id)).futureValue
    }
  }

  it should "add new customer" in {
    //Given
    val name = "awesome_customer"
    val customer = newCustomer(name, user.id)

    //When
    customerDao.add(customer).futureValue

    //Then
    customerDao.findById(customer.id).futureValue should be ('defined)
  }

  it should "delete customer by id" in {
    //Given
    val name = "awesome_customer"
    val customer = newCustomer(name, user.id)

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
    val customer = newCustomer(name, user.id)
    val modifiedCustomer = Customer(customer.id, newName, user.id)

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
    val customer = newCustomer(name, user.id)

    //When
    customerDao.add(customer).futureValue

    //Then
    customerDao.findByName(customer.name).futureValue should be ('defined)
  }

  it should "fetch only data belonging to user" in {
    //given
    val user1 = newRandomStoredUser()
    val customer1 = newRandomStoredCustomer(user1.id)

    //when
    val result = customerDao.allForUser(user1.id).futureValue

    //then
    result.size shouldEqual 1
    result.head shouldEqual customer1
  }

}

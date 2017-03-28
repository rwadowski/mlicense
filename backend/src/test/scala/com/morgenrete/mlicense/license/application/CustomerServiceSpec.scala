package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpersWithDb}
import com.morgenrete.mlicense.user.domain.User
import org.scalatest.Matchers

/**
  * Created by rwadowski on 27.03.17.
  */
class CustomerServiceSpec extends FlatSpecWithDb with Matchers with TestHelpersWithDb {

  val user: User = newRandomUser()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    userDao.add(user).futureValue
    customerDao.add(newCustomer("cus1", user.id)).futureValue
    customerDao.add(newCustomer("cus2", user.id)).futureValue
  }

  "create" should "create customer with name that not exists" in {
    //given
    val name = "cus3"
    val cusCreate = newCreateCustomer(name, user.id)

    //when
    val result = customerService.create(cusCreate).futureValue

    //then
    result should be (CreateCustomerResult.Success)
    customerDao.findByName(name).futureValue should be ('defined)
  }

  "update" should "update existing customers" in {
    //given
    val newName = "cus3_new"
    val customer = newRandomStoredCustomer(user.id)
    val updateCustomer = newUpdateCustomer(newName, user.id, Some(customer.id))

    //when
    val result = customerService.update(updateCustomer).futureValue

    //then
    result should be (UpdateCustomerResult.Success)
    customerDao.findByName(customer.name).futureValue should not be ('defined)
    customerDao.findByName(newName).futureValue should be ('defined)
    customerDao.findById(customer.id).futureValue should be ('defined)
  }

  "update" should "fail during update non existing customers" in {
    //given
    val newName = "cus3_new"
    val customer = newRandomStoredCustomer(user.id)
    val updateCustomer = newUpdateCustomer(newName, user.id)

    //when
    val result = customerService.update(updateCustomer).futureValue

    //then
    result should be(UpdateCustomerResult.CustomerNotExists)
    customerDao.findByName(customer.name).futureValue should be('defined)
    customerDao.findById(customer.id).futureValue should be('defined)
    customerDao.findByName(newName).futureValue should not be ('defined)
    customerDao.findById(updateCustomer.id).futureValue should not be ('defined)
  }

}

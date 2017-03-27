package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpersWithDb}
import org.scalatest.Matchers

/**
  * Created by rwadowski on 27.03.17.
  */
class CustomerServiceSpec extends FlatSpecWithDb with Matchers with TestHelpersWithDb {

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    customerDao.add(newCustomer("cus1"))
    customerDao.add(newCustomer("cus2"))
  }

  "create" should "create customer with name that not exists" in {
    //given
    val name = "cus3"
    val cusCreate = newCreateCustomer(name)

    //when
    val result = customerService.create(cusCreate).futureValue

    //then
    result should be (CreateCustomerResult.Success)
    customerDao.findByName(name).futureValue should be ('defined)
  }

  "create" should "not create customer with name that not exists" in {
    //given
    val name = "cus1"
    val cusCreate = newCreateCustomer(name)

    //when
    val result = customerService.create(cusCreate).futureValue

    //then
    result should be (CreateCustomerResult.CustomerExists)
    customerDao.findByName(name).futureValue should be ('defined)
  }

  "update" should "update existing customers" in {
    //given
    val newName = "cus3_new"
    val customer = newRandomStoredCustomer
    val updateCustomer = newUpdateCustomer(newName, Some(customer.id))

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
    val customer = newRandomStoredCustomer
    val updateCustomer = newUpdateCustomer(newName)

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

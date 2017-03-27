package com.morgenrete.mlicense.license.domain

import java.util.UUID

import com.morgenrete.mlicense.license.CustomerId

/**
  * Created by rwadowski on 20.03.17.
  */
case class Customer(id: CustomerId, name: String)

object Customer {
  def withRandomUUID(name: String): Customer = Customer(UUID.randomUUID(), name)
}


case class CreateCustomer(name: String) {

  lazy val toCustomer: Customer = Customer.withRandomUUID(name)
}

case class UpdateCustomer(id: CustomerId, name: String) {

  lazy val toCustomer: Customer = Customer(id, name)
}
package com.morgenrete.mlicense.license.domain

import java.util.UUID

import com.morgenrete.mlicense.license.CustomerId
import com.morgenrete.mlicense.user.UserId

/**
  * Created by rwadowski on 20.03.17.
  */
case class Customer(id: CustomerId, name: String, userId: UserId)

object Customer {
  def withRandomUUID(name: String, userId: UserId): Customer = Customer(UUID.randomUUID(), name, userId)
}

case class CreateCustomer(name: String) {

  def toCustomer(userId: UserId): Customer = Customer.withRandomUUID(name, userId)
}

case class UpdateCustomer(id: CustomerId, name: String) {

  def toCustomer(userId: UserId): Customer = Customer(id, name, userId)
}
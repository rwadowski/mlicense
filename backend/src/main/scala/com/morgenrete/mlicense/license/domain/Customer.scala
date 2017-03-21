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

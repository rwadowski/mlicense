package com.morgenrete.mlicense.license.domain

import java.util.UUID

import com.morgenrete.mlicense.license.ApplicationId

/**
  * Created by rwadowski on 20.03.17.
  */
case class Application(id: ApplicationId, name: String)

object Application {

  def withRandomUUID(name: String): Application = Application(UUID.randomUUID(), name)
}
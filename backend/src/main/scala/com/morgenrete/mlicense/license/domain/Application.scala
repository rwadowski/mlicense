package com.morgenrete.mlicense.license.domain

import java.util.UUID

import com.morgenrete.mlicense.license.ApplicationId
import com.morgenrete.mlicense.user.UserId

/**
  * Created by rwadowski on 20.03.17.
  */
case class Application(id: ApplicationId, name: String, userId: UserId)

object Application {

  def withRandomUUID(name: String, userId: UserId): Application = Application(UUID.randomUUID(), name, userId)
}

case class CreateApplication(name: String, userId: UserId) {

  lazy val toApplication: Application = Application.withRandomUUID(name, userId)
}

case class UpdateApplication(id: ApplicationId, name: String, userId: UserId) {

  lazy val toApplication: Application = Application(id, name, userId)
}
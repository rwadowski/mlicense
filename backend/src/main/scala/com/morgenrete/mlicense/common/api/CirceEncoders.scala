package com.morgenrete.mlicense.common.api

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes.ClientError
import io.circe._
import io.circe.syntax._

/**
  * Created by rwadowski on 3/4/17. Taken from http://bootzooka.softwaremill.com/#/
  */
trait CirceEncoders {

  val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME

  implicit object DateTimeEncoder extends Encoder[OffsetDateTime] {
    override def apply(dt: OffsetDateTime): Json = dateTimeFormat.format(dt).asJson
  }

  implicit object UuidEncoder extends Encoder[UUID] {
    override def apply(u: UUID): Json = u.toString.asJson
  }

  implicit object ClientErrorEncoder extends Encoder[ClientError] {
    override def apply(a: ClientError): Json = a.defaultMessage.asJson
  }
}

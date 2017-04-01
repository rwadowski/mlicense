package com.morgenrete.mlicense.common.api

import java.time.OffsetDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes.ClientError
import io.circe.Decoder.Result
import io.circe._
import io.circe.syntax._

trait CirceEncoders {

  val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME

  implicit object DateTimeEncoder extends Encoder[OffsetDateTime] {
    override def apply(dt: OffsetDateTime): Json = dateTimeFormat.format(dt).asJson
  }

  implicit object DateTimeDecoder extends Decoder[OffsetDateTime] {
    override def apply(c: HCursor): Result[OffsetDateTime] = {
      c.as[String] match {
        case Right(s) => try Right(OffsetDateTime.parse(s, dateTimeFormat)) catch {
          case _: DateTimeParseException => Left(DecodingFailure("OffsetDateTime", c.history))
        }
        case l @ Left(_) => l.asInstanceOf[Decoder.Result[OffsetDateTime]]
      }
    }
  }

  implicit object UuidEncoder extends Encoder[UUID] {
    override def apply(u: UUID): Json = u.toString.asJson
  }

  implicit object ClientErrorEncoder extends Encoder[ClientError] {
    override def apply(a: ClientError): Json = a.defaultMessage.asJson
  }
}

package com.morgenrete.mlicense.common.api

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{HttpCharsets, MediaTypes}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import io.circe._
import io.circe.jawn.decode

trait JsonSupport extends CirceEncoders {

  implicit def materializer: Materializer

  implicit def circeUnmarshaller[A <: Product: Manifest](implicit d: Decoder[A]): FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        decode[A](input) match {
          case Right(obj) => obj
          case Left(failure) => throw new IllegalArgumentException(failure.getMessage, failure.getCause)
        }
      }

  implicit def circeMarshaller[A <: AnyRef](implicit e: Encoder[A], cbs: CanBeSerialized[A]): ToEntityMarshaller[A] = {
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`) {
      e(_).noSpaces
    }
  }

  /**
    * To limit what data can be serialized to the client, only classes of type `T` for which an implicit
    * `CanBeSerialized[T]` value is in scope will be allowed. You only need to provide an implicit for the base value,
    * any containers like `List` or `Option` will be automatically supported.
    */
  trait CanBeSerialized[T]

  object CanBeSerialized {
    def apply[T] = new CanBeSerialized[T] {}
    implicit def listCanBeSerialized[T](implicit cbs: CanBeSerialized[T]): CanBeSerialized[List[T]] = null
    implicit def setCanBeSerialized[T](implicit cbs: CanBeSerialized[T]): CanBeSerialized[Set[T]] = null
    implicit def optionCanBeSerialized[T](implicit cbs: CanBeSerialized[T]): CanBeSerialized[Option[T]] = null
  }

}
package com.morgenrete.mlicense.license.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.morgenrete.mlicense.common.api.{JsonSupport, SessionSupport}
import com.morgenrete.mlicense.license.application.{CreateCustomerResult, CustomerService, UpdateCustomerResult}
import com.morgenrete.mlicense.license.domain.{CreateCustomer, Customer, UpdateCustomer}
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._

/**
  * Created by rwadowski on 28.03.17.
  */
trait CustomersRoutes extends JsonSupport with SessionSupport with StrictLogging {

  def customerService: CustomerService

  implicit val customerCbs = CanBeSerialized[Customer]

  val customersRoutes = pathPrefix("customers") {
    userIdFromSession { userId =>
      path(JavaUUID) { customerId =>
        pathEndOrSingleSlash {
          get {
            onSuccess(customerService.findById(customerId))  {
              case None => complete(StatusCodes.NotFound)
              case Some(cus) => complete(cus)
            }
          }
        }
      } ~ pathEndOrSingleSlash {
        get {
          onSuccess(customerService.allForUser(userId)) { customers =>
            complete(customers.toList)
          }
        } ~ post {
          entity(as[CreateCustomer]) { in =>
            onSuccess(customerService.create(in.toCustomer(userId))) {
              case CreateCustomerResult.Success => complete(StatusCodes.OK)
              case CreateCustomerResult.CustomerExists => complete(StatusCodes.Conflict, "customer exists")
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        } ~ patch {
          entity(as[UpdateCustomer]) { in =>
            onSuccess(customerService.update(in.toCustomer(userId))) {
              case UpdateCustomerResult.Success => complete(StatusCodes.OK)
              case UpdateCustomerResult.CustomerNotExists => complete(StatusCodes.Conflict, "there is no such customer")
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  }
}

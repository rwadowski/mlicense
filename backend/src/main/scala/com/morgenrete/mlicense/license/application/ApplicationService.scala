package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.license.ApplicationId
import com.morgenrete.mlicense.license.domain.Application
import com.morgenrete.mlicense.user.UserId

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 21.03.17.
  */
class ApplicationService(applicationDao: ApplicationDao)(implicit val ec: ExecutionContext) {

  def findById(applicationId: ApplicationId): Future[Option[Application]] = {
    applicationDao.findById(applicationId)
  }

  def create(app: Application): Future[CreateApplicationResult] = {
    checkApplicationExistence(app.name)(applicationDao.findByName).flatMap{
      case Left(_) => Future.successful(CreateApplicationResult.ApplicationExists)
      case Right(_) =>
        val result = applicationDao.add(app)
        result.map{_ => CreateApplicationResult.Success}
    }
  }

  private def checkApplicationExistence[T](criteria: T)(method: T => Future[Option[Application]]): Future[Either[String, Unit]] = {
    method(criteria).map{
      case Some(_) => Left("Application with this name already exists")
      case _ => Right((): Unit)
    }
  }

  def deleteById(applicationId: ApplicationId): Future[Unit] = {
    applicationDao.delete(applicationId)
  }

  def update(app: Application): Future[UpdateApplicationResult] = {
    checkApplicationExistence(app.id)(applicationDao.findById).flatMap{
      case Left(_) =>
        val result = applicationDao.update(app)
        result.map{_ => UpdateApplicationResult.Success}
      case Right(_) => Future.successful(UpdateApplicationResult.ApplicationNotExists)
    }
  }

  def allForUser(userId: UserId): Future[Seq[Application]] = {
    applicationDao.allForUser(userId)
  }
}

sealed trait CreateApplicationResult

object CreateApplicationResult {

  case object Success extends CreateApplicationResult

  case object ApplicationExists extends CreateApplicationResult

  case class InvalidData(msg: String) extends CreateApplicationResult

}

sealed trait UpdateApplicationResult

object UpdateApplicationResult {

  case object Success extends UpdateApplicationResult

  case object ApplicationNotExists extends UpdateApplicationResult

  case class InvalidData(msg: String) extends UpdateApplicationResult
}
package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.license.LicenseId
import com.morgenrete.mlicense.license.domain.{CreateLicense, License, UpdateLicense}
import com.morgenrete.mlicense.user.UserId

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rwadowski on 27.03.17.
  */
class LicenseService(licenseDao: LicenseDao)(implicit val ec: ExecutionContext) {

  def findById(licenseId: LicenseId): Future[Option[License]] = {
    licenseDao.findById(licenseId)
  }

  def create(license: License): Future[CreateLicenseResult] = {
    licenseDao.add(license).map{_ => CreateLicenseResult.Success}
  }

  private def checkLicenseExistence[T](criteria: T)(method: T => Future[Option[License]]): Future[Either[String, Unit]] = {
    method(criteria).map{
      case Some(_) => Left(s"License with this $criteria exists")
      case _ => Right((): Unit)
    }
  }

  def update(license: License): Future[UpdateLicenseResult] = {
    checkLicenseExistence(license.id)(licenseDao.findById).flatMap{
      case Left(_) =>
        val result = licenseDao.update(license)
        result.map{_ => UpdateLicenseResult.Success}
      case Right(_) => Future.successful(UpdateLicenseResult.LicenseNotExists)
    }
  }

  def allForUser(userId: UserId): Future[Seq[License]] = {
    licenseDao.allForUser(userId)
  }
}

sealed trait CreateLicenseResult

object CreateLicenseResult {

  case object Success extends CreateLicenseResult

  case object LicenseExists extends CreateLicenseResult

  case class InvalidData(msg: String) extends CreateLicenseResult
}

sealed trait UpdateLicenseResult

object UpdateLicenseResult {

  case object Success extends UpdateLicenseResult

  case object LicenseNotExists extends UpdateLicenseResult

  case class InvalidData(msg: String) extends UpdateLicenseResult
}

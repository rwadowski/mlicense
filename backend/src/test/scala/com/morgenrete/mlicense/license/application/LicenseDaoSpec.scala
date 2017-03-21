package com.morgenrete.mlicense.license.application

import com.morgenrete.mlicense.license.domain.License
import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpers, TestHelpersWithDb}
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext

/**
  * Created by rwadowski on 21.03.17.
  */
class LicenseDaoSpec extends FlatSpecWithDb with StrictLogging with TestHelpersWithDb with Matchers {
  behavior of "LicenseDao"

  val licenseDao = new LicenseDao(sqlDatabase)

  it should "add new license" in {
    //Given
    val user = newRandomStoredUser(Some("password"))
    val customer = newRandomStoredCustomer
    val application = newRandomStoredApplication

    val license = License.withRandomUUID(user.id, application.id, customer.id, true, currentOffsetDateTime)

    //When
    licenseDao.add(license).futureValue

    //Then
    licenseDao.findById(license.id).futureValue should be ('defined)
  }

  it should "delete license by id" in {
    //Given
    val user = newRandomStoredUser(Some("password"))
    val customer = newRandomStoredCustomer
    val application = newRandomStoredApplication

    val license = License.withRandomUUID(user.id, application.id, customer.id, true, currentOffsetDateTime)

    //When
    licenseDao.add(license).futureValue
    licenseDao.delete(license.id).futureValue

    //Then
    licenseDao.findById(license.id).futureValue should not be ('defined)
  }

  it should "update license" in {
    //Given
    val user = newRandomStoredUser(Some("password"))
    val customer = newRandomStoredCustomer
    val application = newRandomStoredApplication

    val license = License.withRandomUUID(user.id, application.id, customer.id, true, currentOffsetDateTime)
    val newExpirationDate = validExpirationDate(10) //ten days from now it will expire
    val modifiedLicense = license.copy(expirationDate =  newExpirationDate)

    //When
    licenseDao.add(license).futureValue
    licenseDao.update(modifiedLicense).futureValue

    //Then
    licenseDao.findById(license.id).futureValue should be ('defined)
    licenseDao.findById(license.id).futureValue shouldEqual Some(modifiedLicense)
  }
}

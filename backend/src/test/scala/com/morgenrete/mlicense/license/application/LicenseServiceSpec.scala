package com.morgenrete.mlicense.license.application

import java.util.UUID

import com.morgenrete.mlicense.license.domain.{Application, Customer, License}
import com.morgenrete.mlicense.test.{FlatSpecWithDb, TestHelpersWithDb}
import com.morgenrete.mlicense.user.domain.User
import org.scalatest.Matchers

/**
  * Created by rwadowski on 27.03.17.
  */
class LicenseServiceSpec extends FlatSpecWithDb with Matchers with TestHelpersWithDb {

  val user: User = newRandomUser()
  val customer1: Customer = newCustomer("cus1", user.id)
  val customer2: Customer = newCustomer("cus2", user.id)
  val application: Application = newApplication("app1", user.id)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    userDao.add(user).futureValue
    customerDao.add(customer1).futureValue
    customerDao.add(customer2).futureValue
    applicationDao.add(application).futureValue
    val license1 = License.withRandomUUID(user.id, application.id, customer1.id, true, currentOffsetDateTime, "name")
    val license2 = License.withRandomUUID(user.id, application.id, customer2.id, true, currentOffsetDateTime, "name")
    licenseDao.add(license1).futureValue
    licenseDao.add(license2).futureValue
  }

  "create" should "create license" in {
    //given
    val customer3 = newRandomStoredCustomer(user.id)
    val licCreate = newCreateLicense(application.id, customer3.id, true, validExpirationDate(2))

    //when
    val result = licenseService.create(licCreate.toLicense(user.id)).futureValue

    //then
    result should be (CreateLicenseResult.Success)
    licenseService.findById(licCreate.toLicense(user.id).id).futureValue should be ('defined)
  }

  "update" should "update existing licenses" in {
    //given
    val license = newRandomStoredLicense(Some(user))
    val updateLicense = newUpdateLicense(license.id, license.applicationId, license.customerId, false, license.expirationDate, "name")

    //when
    val result = licenseService.update(updateLicense.toLicense(user.id)).futureValue

    //then
    result should be (UpdateLicenseResult.Success)
    licenseDao.findById(license.id).futureValue should be ('defined)
    val expected = Some(License(license.id, license.userId, license.applicationId, license.customerId, false, license.expirationDate, "name"))
    licenseDao.findById(license.id).futureValue shouldEqual expected
  }

  "update" should "fail during update non existing applications" in {
    //given
    val license = newRandomStoredLicense(Some(user))
    val updateLicense = newUpdateLicense(UUID.randomUUID(), license.applicationId, license.customerId, false, validExpirationDate(1), "name")

    //when
    val result = licenseService.update(updateLicense.toLicense(user.id)).futureValue

    //then
    result should be (UpdateLicenseResult.LicenseNotExists)
    licenseDao.findById(license.id).futureValue should be ('defined)
    licenseDao.findById(updateLicense.toLicense(user.id).id).futureValue should not be ('defined)
  }
}

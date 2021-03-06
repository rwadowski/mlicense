package com.morgenrete.mlicense.test

import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID

import com.morgenrete.mlicense.license.{ApplicationId, CustomerId, LicenseId}
import com.morgenrete.mlicense.license.domain._
import com.morgenrete.mlicense.user.UserId
import com.morgenrete.mlicense.user.domain.User

trait TestHelpers {

  val createdOn: OffsetDateTime = OffsetDateTime.of(2015, 6, 3, 13, 25, 3, 0, ZoneOffset.UTC)

  lazy val randomIds: Seq[UUID] = List.fill(3)(UUID.randomUUID())

  private val random = new scala.util.Random
  private val characters = "abcdefghijklmnopqrstuvwxyz0123456789"

  def randomString(length: Int = 10): String = Stream.continually(random.nextInt(characters.length)).map(characters).take(length).mkString

  def newUser(login: String, email: String, pass: String, salt: String): User =
    User.withRandomUUID(login, email, pass, salt, createdOn)

  def newRandomUser(password: Option[String] = None): User = {
    val login = randomString()
    val pass = password.getOrElse(randomString())
    newUser(login, s"$login@example.com", pass, "someSalt")
  }

  def newApplication(name: String, userId: UserId): Application = Application.withRandomUUID(name, userId)

  def newRandomApplication(userId: UserId): Application = {
    val name = randomString()
    Application.withRandomUUID(name, userId)
  }

  def newCreateApplication(name: String): CreateApplication = CreateApplication(name)
  def newUpdateApplication(name: String, id: Option[ApplicationId] = None): UpdateApplication = UpdateApplication(id.getOrElse(UUID.randomUUID()), name)

  def newCustomer(name: String, userId: UserId): Customer = Customer.withRandomUUID(name, userId)

  def newRandomCustomer(userId: UserId): Customer = {
    val name = randomString()
    Customer.withRandomUUID(name, userId)
  }

  def newCreateCustomer(name: String): CreateCustomer = CreateCustomer(name)
  def newUpdateCustomer(name: String, id: Option[CustomerId] = None): UpdateCustomer = UpdateCustomer(id.getOrElse(UUID.randomUUID()), name)

  def newLicense(userId: UserId,
                 applicationId: ApplicationId,
                 customerId: CustomerId,
                 active: Boolean,
                 expirationDate: OffsetDateTime,
                 name: String): License = License.withRandomUUID(userId, applicationId, customerId, active, expirationDate, name)

  def newRandomLicense(userId: UserId,
                       applicationId: ApplicationId,
                       customerId: CustomerId): License = License.withRandomUUID(userId, applicationId, customerId, true, validExpirationDate(100), randomString())

  def newCreateLicense(applicationId: ApplicationId,
                       customerId: CustomerId,
                       active: Boolean,
                       expirationDate: OffsetDateTime): CreateLicense = CreateLicense(applicationId,
                                                                                      customerId,
                                                                                      active,
                                                                                      expirationDate,
                                                                                      randomString())

  def newUpdateLicense(licenseId: LicenseId,
                       applicationId: ApplicationId,
                       customerId: CustomerId,
                       active: Boolean,
                       expirationDate: OffsetDateTime,
                       name: String): UpdateLicense = UpdateLicense(licenseId,
                                                                    applicationId,
                                                                    customerId,
                                                                    active,
                                                                    expirationDate,
                                                                    name)

  def validExpirationDate(days: Int): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(days)
  def invalidExpirationDate(days: Int): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).minusDays(days)
  def currentOffsetDateTime: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}

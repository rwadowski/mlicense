package com.morgenrete.mlicense.test

import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID

import com.morgenrete.mlicense.license.{ApplicationId, CustomerId}
import com.morgenrete.mlicense.license.domain.{Application, Customer, License}
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

  def newApplication(name: String): Application = Application.withRandomUUID(name)

  def newRandomApplication: Application = {
    val name = randomString()
    Application.withRandomUUID(name)
  }

  def newCustomer(name: String): Customer = Customer.withRandomUUID(name)

  def newRandomCustomer: Customer = {
    val name = randomString()
    Customer.withRandomUUID(name)
  }

  def newLicense(userId: UserId,
                 applicationId: ApplicationId,
                 customerId: CustomerId,
                 active: Boolean,
                 expirationDate: OffsetDateTime): License = License.withRandomUUID(userId, applicationId, customerId, active, expirationDate)

  def validExpirationDate(days: Int): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(days)
  def invalidExpirationDate(days: Int): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).minusDays(days)
  def currentOffsetDateTime: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}

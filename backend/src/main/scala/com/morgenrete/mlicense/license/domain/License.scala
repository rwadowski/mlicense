package com.morgenrete.mlicense.license.domain

import java.time.OffsetDateTime
import java.util.UUID

import com.morgenrete.mlicense.license.{ApplicationId, CustomerId, LicenseId}
import com.morgenrete.mlicense.user.UserId

/**
  * Created by rwadowski on 20.03.17.
  */
//TODO true/false -> case class
case class License(id: LicenseId,
                   userId: UserId,
                   applicationId: ApplicationId,
                   customerId: CustomerId,
                   active: Boolean,
                   expirationDate: OffsetDateTime,
                   name: String)

object License {
  def withRandomUUID(userId: UserId,
                     applicationId: ApplicationId,
                     customerId: CustomerId,
                     active: Boolean,
                     expirationDate: OffsetDateTime,
                     name: String): License =
    License(UUID.randomUUID(),
            userId,
            applicationId,
            customerId,
            active,
            expirationDate,
            name)
}

case class CreateLicense(applicationId: ApplicationId,
                         customerId: CustomerId,
                         active: Boolean,
                         expirationDate: OffsetDateTime,
                         name: String) {

  def toLicense(userId: UserId): License = License.withRandomUUID(userId, applicationId, customerId, active, expirationDate, name)
}

case class UpdateLicense(id: LicenseId,
                         applicationId: ApplicationId,
                         customerId: CustomerId,
                         active: Boolean,
                         expirationDate: OffsetDateTime,
                         name: String) {

  def toLicense(userId: UserId): License = License(id, userId, applicationId, customerId, active, expirationDate, name)
}
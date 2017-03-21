package com.morgenrete.mlicense.license.domain

import java.time.OffsetDateTime
import java.util.UUID

import com.morgenrete.mlicense.license.{ApplicationId, CustomerId, LicenseId}
import com.morgenrete.mlicense.user.UserId

/**
  * Created by rwadowski on 20.03.17.
  */
//TODO true/false -> class
case class License(id: LicenseId,
                   userId: UserId,
                   applicationId: ApplicationId,
                   customerId: CustomerId,
                   active: Boolean,
                   expirationDate: OffsetDateTime)

object License {
  def withRandomUUID(userId: UserId,
                     applicationId: ApplicationId,
                     customerId: CustomerId,
                     active: Boolean,
                     expirationDate: OffsetDateTime): License =
    License(UUID.randomUUID(),
            userId,
            applicationId,
            customerId,
            active,
            expirationDate)
}
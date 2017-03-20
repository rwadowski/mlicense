package com.morgenrete.mlicense.license.domain

import java.time.OffsetDateTime

import com.morgenrete.mlicense.license.{ApplicationId, CustomerId, LicenseId}
import com.morgenrete.mlicense.user.UserId

/**
  * Created by rwadowski on 20.03.17.
  */
case class License(id: LicenseId,
                   userId: UserId,
                   applicationId: ApplicationId,
                   customerId: CustomerId,
                   active: Boolean,
                   expirationDate: OffsetDateTime)

package com.morgenrete.mlicense.email.application

import com.morgenrete.mlicense.email.domain.EmailContentWithSubject
import org.scalatest.{FlatSpec, Matchers}

class DummyEmailSendingServiceSpec extends FlatSpec with Matchers {
  it should "send scheduled email" in {
    val service = new DummyEmailService
    service.scheduleEmail("test@sml.com", EmailContentWithSubject("content", "subject"))
    service.wasEmailSent("test@sml.com", "subject") should be(true)
    service.wasEmailSentTo("test@sml.com") should be(true)
    service.wasEmailSentTo("other@sml.com") should be(false)
  }
}

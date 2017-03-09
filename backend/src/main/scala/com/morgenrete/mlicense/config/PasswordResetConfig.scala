package com.morgenrete.mlicense.config

import com.typesafe.config.Config

trait PasswordResetConfig {
  def rootConfig: Config

  lazy val resetLinkPattern = rootConfig.getString("mlicense.reset-link-pattern")
}

package com.morgenrete.mlicense.config

import com.typesafe.config.Config

trait LicenseConfig {
  def rootConfig: Config
}

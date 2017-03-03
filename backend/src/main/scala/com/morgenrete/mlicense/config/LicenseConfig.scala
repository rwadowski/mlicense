package com.morgenrete.mlicense.config

import com.typesafe.config.Config

/**
  * Created by rwadowski on 3/2/17.
  */
trait LicenseConfig {
  def rootConfig: Config
}

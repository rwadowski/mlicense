package com.morgenrete.mlicense.config

import com.typesafe.config.Config

/**
  * Created by rwadowski on 3/5/17.
  */
class MLicenseConfig(override val rootConfig: Config) extends DatabaseConfig with ServerConfig with LicenseConfig

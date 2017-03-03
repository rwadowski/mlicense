package com.morgenrete.mlicense.config

import com.typesafe.config.Config

/**
  * Created by rwadowski on 3/2/17.
  */
trait ServerConfig {
  def rootConfig: Config

  lazy val host: String = rootConfig.getString("server.host")
  lazy val port: Int = rootConfig.getInt("server.port")
}

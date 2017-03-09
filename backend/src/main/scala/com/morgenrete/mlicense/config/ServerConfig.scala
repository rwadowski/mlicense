package com.morgenrete.mlicense.config

import com.typesafe.config.Config

trait ServerConfig {
  def rootConfig: Config

  lazy val host: String = rootConfig.getString("server.host")
  lazy val port: Int = rootConfig.getInt("server.port")
}

package com.morgenrete.mlicense.config

import com.morgenrete.mlicense.common.api.ConfigWithDefaults
import com.typesafe.config.Config

trait DatabaseConfig extends ConfigWithDefaults {
  import DatabaseConfig._

  def rootConfig: Config

  lazy val dbH2Url: String              = getString("mlicense.db.h2.properties.url", "jdbc:h2:file:./data/mlicense")
  lazy val dbPostgresServerName:String  = getString(postgresServerNameKey, "")
  lazy val dbPostgresPort: String       = getString(postgresPortKey, "5432")
  lazy val dbPostgresDbName: String     = getString(postgresDbNameKey, "")
  lazy val dbPostgresUsername: String   = getString(postgresUsernameKey, "")
  lazy val dbPostgresPassword: String   = getString(postgresPasswordKey, "")
}

object DatabaseConfig {
  val postgresDSClass       = "mlicense.db.postgres.dataSourceClass"
  val postgresServerNameKey = "mlicense.db.postgres.properties.serverName"
  val postgresPortKey       = "mlicense.db.postgres.properties.portNumber"
  val postgresDbNameKey     = "mlicense.db.postgres.properties.databaseName"
  val postgresUsernameKey   = "mlicense.db.postgres.properties.user"
  val postgresPasswordKey   = "mlicense.db.postgres.properties.password"
}
package com.morgenrete.mlicense.common.api

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

/**
  * Created by rwadowski on 3/2/17.
  */
trait ConfigWithDefaults {

  def rootConfig: Config

  def getBoolean(path: String, default: Boolean): Boolean = ifHasPath(path, default) { _.getBoolean(path) }
  def getString(path: String, default: String): String = ifHasPath(path, default) { _.getString(path) }
  def getInt(path: String, default: Int): Int = ifHasPath(path, default) { _.getInt(path) }
  def getConfig(path: String, default: Config): Config = ifHasPath(path, default) { _.getConfig(path) }
  def getMilliseconds(path: String, default: Long): Long = ifHasPath(path, default) { _.getDuration(path, TimeUnit.MILLISECONDS) }
  def getOptionalString(path: String, default: Option[String] = None): Option[String] = getOptional(path) { _.getString(path) }

  private def ifHasPath[T](path: String, default: T)(get: Config => T): T = {
    if (rootConfig.hasPath(path)) get(rootConfig) else default
  }

  private def getOptional[T](fullPath: String, default: Option[T] = None)(get: Config => T) = {
    if (rootConfig.hasPath(fullPath)) {
      Some(get(rootConfig))
    }
    else {
      default
    }
  }

}


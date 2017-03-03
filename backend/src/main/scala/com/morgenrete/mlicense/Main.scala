package com.morgenrete.mlicense

import com.typesafe.scalalogging.StrictLogging

/**
  * Created by rwadowski on 3/1/17.
  */
object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    val server = new Server
    server.start()
  }
}

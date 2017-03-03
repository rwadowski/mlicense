package com.morgenrete.mlicense

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.morgenrete.mlicense.config.{DatabaseConfig, LicenseConfig, ServerConfig}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

import scala.util.{Failure, Success}

/**
  * Created by rwadowski on 3/1/17.
  */
class Server extends Routes with StrictLogging {

  private implicit val system = ActorSystem()
  private implicit val materializer = ActorMaterializer()
  import system.dispatcher

  private lazy val config = new DatabaseConfig with LicenseConfig with ServerConfig {
    override val rootConfig: Config = ConfigFactory.load()
  }

  def start(): Unit = {
    val startFuture = Http().bindAndHandle(routes, config.host, config.port)
    startFuture.onComplete {
      case Success(b) =>
        logger.info(s"Server started on ${config.host}:${config.port}")
        sys.addShutdownHook {
          b.unbind()
          stop()
          logger.info("Server stopped")
        }
      case Failure(e) =>
        logger.error(s"Cannot start server on ${config.host}:${config.port}", e)
        sys.addShutdownHook {
          stop()
          logger.info("Server stopped")
        }
    }
  }

  private def stop(): Unit = system.terminate()
}

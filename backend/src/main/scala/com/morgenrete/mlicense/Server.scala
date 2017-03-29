package com.morgenrete.mlicense

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import com.morgenrete.mlicense.config.MLicenseConfig
import com.morgenrete.mlicense.user.application.Session
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by rwadowski on 3/1/17.
  */
class Server extends Routes with Wiring with StrictLogging {

  override lazy val config: MLicenseConfig = new MLicenseConfig(ConfigFactory.load())
  lazy val sessionConfig: SessionConfig = SessionConfig.fromConfig(config.rootConfig).copy(sessionEncryptData = true)

  implicit val system = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatchers.lookup("akka-http-routes-dispatcher")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val sessionManager: SessionManager[Session] = new SessionManager[Session](sessionConfig)

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

package com.github.unisay.test.kit.service

import com.typesafe.scalalogging.StrictLogging
import org.mockserver.client.server.ForwardChainExpectation
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import scala.concurrent.duration._
import scala.language.postfixOps

class MockServerTestService(override val name: String, val host: String = "localhost", val port: Int = 0)
  extends TestService with StrictLogging {

  protected var maybeMockServer: Option[ClientAndServer] = None

  def client: ClientAndServer = {
    if (maybeMockServer.isDefined)
      maybeMockServer.get
    else
      throw new IllegalStateException(s"MockServer ($name) is not initialized")
  }

  def url: String = s"http://$host:${client.getPort}"

  def when(request: HttpRequest): ForwardChainExpectation =
    client.when(request, Times.unlimited())

  def when(path: String): ForwardChainExpectation =
    when(HttpRequest.request(path))

  def start(): Unit = {
    logger.info("Starting {}", name)
    maybeMockServer = Some(ClientAndServer.startClientAndServer(port))
    logger.info("{} started", name)
  }

  def reset(): Unit =
    maybeMockServer.foreach(_.reset())

  def stop(): Unit = {
    maybeMockServer.foreach { mockServer =>
      logger.info("Stopping {}", name)
      mockServer.stop()
      logger.info("{} stopped", name)
    }
  }

  def verifyNoInteractions(duration: FiniteDuration = 0 millis) = {
    if (duration.toMillis > 0)
      Thread.sleep(duration.toMillis)
    client.verifyZeroInteractions()
  }

}

package org.zalando.test.kit.service

import com.typesafe.scalalogging.StrictLogging
import org.mockserver.client.server.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.scalatest.concurrent.AsyncAssertions

class MockServerTestService(override val name: String, val host: String = "localhost", val port: Int = 0)
  extends TestService with AsyncAssertions with StrictLogging with ExpectationVerification {

  protected var maybeMockServer: Option[ClientAndServer] = None

  def url: Option[String] = maybeMockServer.map(cas ⇒ s"http://$host:${cas.getPort}")

  protected def mockServer: MockServerClient = {
    if (maybeMockServer.isDefined)
      maybeMockServer.get
    else
      throw new IllegalStateException("MockServer is not initialized")
  }

  def start(): Unit = {
    logger.info("Starting {}", name)
    maybeMockServer = Some(ClientAndServer.startClientAndServer(port))
    logger.info("{} started", name)
  }

  def stop(): Unit = {
    maybeMockServer.foreach { mockServer =>
      logger.info("Stopping {}", name)
      mockServer.stop()
      logger.info("{} stopped", name)
    }
  }

}

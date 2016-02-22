package org.zalando.test.kit.service

import com.typesafe.scalalogging.StrictLogging
import org.mockserver.client.server.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.scalatest.concurrent.AsyncAssertions

class MockServerTestService(override val name: String, val port: Int, val host: String = "localhost")
  extends TestService with SuiteLifecycle with AsyncAssertions with StrictLogging with ExpectationVerification {

  protected var maybeMockServer: Option[MockServerClient] = None

  val apiUrl = s"http://$host:$port"

  protected def mockServer: MockServerClient = {
    if (maybeMockServer.isDefined)
      maybeMockServer.get
    else
      throw new IllegalStateException("MockServer is not initialized")
  }

  override def start(): Unit = {
    logger.info("Starting {}", name)
    maybeMockServer = Some(ClientAndServer.startClientAndServer(port))
    logger.info("{} started", name)
  }

  override def stop(): Unit = {
    maybeMockServer.foreach { mockServer =>
      logger.info("Stopping {}", name)
      mockServer.stop()
      logger.info("{} stopped", name)
    }
  }

}

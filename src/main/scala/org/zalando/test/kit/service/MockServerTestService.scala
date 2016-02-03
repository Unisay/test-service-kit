package org.zalando.test.kit.service

import org.mockserver.client.server.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.verify.VerificationTimes
import org.mockserver.verify.VerificationTimes._
import org.scalatest.concurrent.AsyncAssertions
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.util.Try

abstract class MockServerTestService(val mockServerPort: Int) extends TestService with AsyncAssertions {

  private val logger = LoggerFactory.getLogger(classOf[MockServerTestService])

  implicit protected var maybeMockServer: Option[MockServerClient] = None

  protected var expectations = ListBuffer[(HttpRequest, VerificationTimes)]()

  protected def mockServer: MockServerClient = {
    if (maybeMockServer.isDefined)
      maybeMockServer.get
    else
      throw new IllegalStateException("MockServer is not initialized. Ensure beforeAll() is invoked in your test")
  }

  override def beforeSuite(): Unit = start()

  override def beforeTest(): Unit = resetExpectations()

  override def afterTest(): Unit = verifyExpectations()

  override def afterSuite(): Unit = stop()

  def start(): Unit = {
    logger.info("Starting {}", name)
    maybeMockServer = Some(ClientAndServer.startClientAndServer(mockServerPort))
    logger.info("{} started", name)
  }

  def stop(): Unit = {
    maybeMockServer.foreach { mockServer =>
      logger.info("Stopping {}", name)
      mockServer.stop()
      logger.info("{} stopped", name)
    }
  }

  def resetExpectations(): Unit = {
    expectations.clear()
    Try(mockServer.reset())
  }

  def verifyExpectations(): Unit =
    expectations.foreach(tuple ⇒ mockServer.verify(tuple._1, tuple._2))

  def expect(request: HttpRequest, times: VerificationTimes = once): Unit =
    expectations += ((request, times))

  def verify(request: HttpRequest, times: VerificationTimes = once): HttpRequest = {
    expect(request, times)
    request
  }

  def expectNoInteractions(): Unit =
    expect(HttpRequest.request, exactly(0))

}

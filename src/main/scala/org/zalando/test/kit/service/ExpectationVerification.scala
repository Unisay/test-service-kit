package org.zalando.test.kit.service

import java.util.concurrent.TimeUnit

import org.mockserver.model.{HttpRequest, HttpResponse}
import org.mockserver.verify.VerificationTimes
import org.mockserver.verify.VerificationTimes._

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.util.Try

trait ExpectationVerification {
  this: MockServerTestService ⇒

  protected var expectations = ListBuffer[(HttpRequest, VerificationTimes)]()

  override def beforeTest(): Unit = resetExpectations()

  override def afterTest(): Unit = verifyExpectations()

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

  def expectSlowResponse(status: Int, duration: Duration): Unit =
    mockServer when HttpRequest.request respond
      HttpResponse.response().withStatusCode(status).withDelay(TimeUnit.MILLISECONDS, duration.toMillis)

  def expectResponseWithStatus(status: Int, times: VerificationTimes = once): Unit =
    mockServer when verify(HttpRequest.request, times) respond
      HttpResponse.response.withStatusCode(status)
}

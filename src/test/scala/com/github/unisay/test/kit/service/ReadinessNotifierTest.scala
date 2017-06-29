package com.github.unisay.test.kit.service

import org.mockserver.verify.VerificationTimes
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FlatSpec, MustMatchers}
import com.github.unisay.test.kit.{SampleResponses, ScalatestServiceKit}
import com.github.unisay.test.kit.service.ReadinessNotifier._
import scala.concurrent.duration._

class ReadinessNotifierTest extends FlatSpec with ScalaFutures with MustMatchers with ScalatestServiceKit {

  val sampleHealthCheck = new MockServerTestService("Sample resource") with SuiteLifecycle with SampleResponses
  override def testServices = sampleHealthCheck

  behavior of "ReadinessNotifier.immediately"

  it must "return future" in {
    whenReady(immediately.whenReady(), Timeout(Span(0, Seconds))) { result =>
      result mustBe Ready
    }
  }

  it must "await ready" in {
    immediately.awaitReady(Duration.Zero) mustBe Ready
  }

  behavior of "ReadinessNotifier.duration"

  it must "return future" in {
    whenReady(duration(100.millis).whenReady(), Timeout(Span(100, Milliseconds)))(_ mustBe Ready)
  }

  it must "await ready" in {
    duration(100.millis).awaitReady(100.millis) mustBe Ready
  }

  behavior of "ReadinessNotifier.healthCheck"

  it must "be ready if url responds with success (2XX)" in {
    sampleHealthCheck.expectResponseWithStatus(200)
    healthCheck(sampleHealthCheck.url, interval = 100.millis).awaitReady(1.second) mustBe Ready
  }

  it must "timeout if url responds with error (not 2XX)" in {
    sampleHealthCheck.expectResponseWithStatus(500)
    the [RuntimeException] thrownBy {
      healthCheck(sampleHealthCheck.url, interval = 200.millis).awaitReady(1.second)
    } must have message s"Resource (${sampleHealthCheck.url}) is not ready within duration (1 second)"
  }

}

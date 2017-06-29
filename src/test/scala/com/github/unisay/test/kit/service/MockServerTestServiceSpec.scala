package com.github.unisay.test.kit.service

import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import com.github.unisay.test.kit.{SampleResponses, ScalatestServiceKit}
import scala.concurrent.ExecutionContext.Implicits.global
import scalaj.http._

/**
  * Sample MockServer based test service
  */
class MockServerTestServiceSpec extends FeatureSpec with GivenWhenThen with MustMatchers with ScalatestServiceKit {

  val sampleRestService1 = new MockServerTestService("Sample REST service mock 1")
    with SuiteLifecycle with SampleResponses

  val sampleRestService2 = new MockServerTestService("Sample REST service mock 2")
    with TestLifecycle with SampleResponses

  override def testServices = sampleRestService1 || sampleRestService2

  scenario("Start sample rest service mock before and stop it after test suite") {
    Given("Mocks respond with 'healthy'")
    sampleRestService1.healthCheckRespondsWith("healthy 1")
    sampleRestService2.healthCheckRespondsWith("healthy 2")

    When("Actual requests are made")
    val response1 = Http(s"${sampleRestService1.url}/health").asString
    val response2 = Http(s"${sampleRestService2.url}/health").asString

    Then("Responses contain data from mocks")
    response1.is2xx mustBe true
    response1.body mustBe "healthy 1"

    response2.is2xx mustBe true
    response2.body mustBe "healthy 2"
  }

  scenario("Reset all expectations before each test") {
    Given("Sample REST service mock has its expectations reset before each test")

    When("Actual request is made")
    val response1 = Http(s"${sampleRestService1.url}/health").asString
    val response2 = Http(s"${sampleRestService2.url}/health").asString

    Then("Response contains no data from previously set expectation")
    response1.is4xx mustBe true
    response2.is4xx mustBe true
  }

}

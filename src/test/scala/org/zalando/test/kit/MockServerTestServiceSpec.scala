package org.zalando.test.kit

import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import org.zalando.test.kit.service.MockServerTestService

import scalaj.http._

/**
  * Sample MockServer based test service
  */
class MockServerTestServiceSpec extends FeatureSpec with GivenWhenThen with MustMatchers with ScalatestServiceKit {

  val sampleRestService1 = new MockServerTestService("Sample REST service mock 1", 8080) with SampleResponses
  val sampleRestService2 = new MockServerTestService("Sample REST service mock 2", 8081) with SampleResponses

  override def testServices = List(sampleRestService1, sampleRestService2)

  scenario("Start sample rest service mock before and stop it after test suite") {
    Given("Mocks respond with 'healthy'")
    sampleRestService1.healthCheckRespondsWith("healthy 1")
    sampleRestService2.healthCheckRespondsWith("healthy 2")

    When("Actual requests are made")
    val response1 = Http(s"${sampleRestService1.apiUrl}/health").asString
    val response2 = Http(s"${sampleRestService2.apiUrl}/health").asString

    Then("Responses contain data from mocks")
    response1.is2xx mustBe true
    response1.body mustBe "healthy 1"

    response2.is2xx mustBe true
    response2.body mustBe "healthy 2"
  }

  scenario("Reset all expectations before each test") {
    Given("Sample REST service mock has its expectations reset before each test")

    When("Actual request is made")
    val response1 = Http(s"${sampleRestService1.apiUrl}/health").asString
    val response2 = Http(s"${sampleRestService2.apiUrl}/health").asString

    Then("Response contains no data from previously set expectation")
    response1.is4xx mustBe true
    response2.is4xx mustBe true
  }

}

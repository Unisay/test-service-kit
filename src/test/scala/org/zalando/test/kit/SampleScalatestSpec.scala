package org.zalando.test.kit

import com.github.kxbmap.configs.syntax._
import com.typesafe.config.ConfigFactory
import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import org.zalando.test.kit.service.DockerTestServiceConfig

import scala.io.Source
import scalaj.http._

/**
  * Sample Scalatest spec to demonstrate usage of test services
  */
class SampleScalatestSpec extends FeatureSpec with GivenWhenThen with MustMatchers with ScalatestServiceKit {

  val config = ConfigFactory.load()
  val sampleRestService = new SampleRestService
  val sampleDockerContainer = new SampleDockerContainer(config.get[DockerTestServiceConfig]("sample-docker-container"))

  override def testServices = List(sampleRestService, sampleDockerContainer)

  feature("MockServerTestService") {

    scenario("Start sample rest service mock before and stop it after test suite") {
      Given("Mock responds with 'healthy'")
      sampleRestService.healthCheckRespondsWith("healthy")

      When("Actual request is made")
      val response = Http(sampleRestService.healthCheckUrl).asString

      Then("Response contains data from mock")
      response.is2xx mustBe true
      response.body mustBe "healthy"
    }

    scenario("Reset all expectations before each test") {
      Given("Sample REST service mock has its expectations reset before each test")

      When("Actual request is made")
      val response = Http(sampleRestService.healthCheckUrl).asString

      Then("Response contains no data from previously set expectation")
      response.is4xx mustBe true
    }

  }

  feature("DockerTestService") {

    scenario("Start sample docker container before and stop it after test suite") {
      Given("Sample docker container exposes resource via HTTP")

      When("Request to the exposed resource is made")
      val response = Http(sampleDockerContainer.sampleResourceUrl).asString

      Then("Successful response contains data from shared folder")
      response.is2xx mustBe true
      response.body mustBe Source.fromFile("src/test/resources/shared_with_docker/resource.htm").mkString
    }

  }


}

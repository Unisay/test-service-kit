package org.zalando.test.kit

import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import org.zalando.test.kit.service.{DockerApiConfig, DockerTestServiceConfig, SharedDirectoryConfig}

import scalaj.http._

/**
  * Sample Scalatest spec to demonstrate usage of test services
  */
class SampleScalatestSpec extends FeatureSpec with GivenWhenThen with MustMatchers with ScalatestServiceKit {

  val sampleRestService = new SampleRestService
  val sampleDockerContainer = new SampleDockerContainer(
    config = new SampleDockerContainerConfig(
        docker = new DockerTestServiceConfig(
        imageNameSubstring = "jetty",
        api = new DockerApiConfig("/var/run/docker.sock", "http://127.0.0.1:2375"),
        portBindings = Set(80, 81),
        shared = new SharedDirectoryConfig(internal = "/shared", external = "/shared"),
        commandLineArguments = Seq.empty
      )
    )
  )

  override def testServices = List(sampleRestService, sampleDockerContainer)

  feature("ScalatestServiceKit") {

    scenario("Start all test services before and stop them after test suite") {
      Given("Sample REST service mock responds with 'healthy'")
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

}

package org.zalando.test.kit

import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import org.zalando.test.kit.service.{DockerApiConfig, DockerTestServiceConfig, SharedDirectoryConfig}

import scala.io.Source
import scalaj.http._

/**
  * Sample Scalatest spec to demonstrate usage of test services
  */
class SampleScalatestSpec extends FeatureSpec with GivenWhenThen with MustMatchers with ScalatestServiceKit {

  val sampleRestService = new SampleRestService
  val sampleDockerContainer = new SampleDockerContainer(
    config = new SampleDockerContainerConfig(
        docker = new DockerTestServiceConfig(
        imageNameSubstring = "gliderlabs/alpine", // minimalistic linux image
        api = new DockerApiConfig("/var/run/docker.sock", "http://127.0.0.1:2375"),
        portBindings = Map(80 → 8888),
        shared = new SharedDirectoryConfig(
          internal = "/www",
          external = getClass.getResource("/shared_with_docker").getFile),
        commandLineArguments = "busybox httpd -f -p 80 -h /www".split("\\s")
      )
    )
  )

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

      Then("Response is successful")
      response.is2xx mustBe true
      response.body mustBe Source.fromFile("src/test/resources/shared_with_docker/resource.htm").mkString
    }

  }



}

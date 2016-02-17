package org.zalando.test.kit

import dispatch.Defaults._
import dispatch._
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import org.zalando.test.kit.service.{DockerApiConfig, DockerTestServiceConfig, SharedDirectoryConfig}
/**
  * Sample Scalatest spec to demonstrate usage of test services
  */
class SampleScalatestSpec extends FlatSpec with ScalatestServiceKit with ScalaFutures {

  val sampleRestService = new SampleRestService
  val sampleDockerContainer = new SampleDockerContainer(
    config = new SampleDockerContainerConfig(
        docker = new DockerTestServiceConfig(
        imageNameSubstring = "jetty",
        api = new DockerApiConfig("/var/run/docker.sock", "http://127.0.0.1:2375"),
        portBindings = Set(80, 81),
        shared = new SharedDirectoryConfig("/tmp", "/tmp"),
        commandLineArguments = Seq("hello")
      )
    )
  )

  override def testServices = List(sampleRestService, sampleDockerContainer)

  "ScalatestServiceKit" should "manage test services" in {
    sampleRestService.healthCheckRespondsWith("healthy")

    val expectedResponse = "healthy"
    val actualResponse = Http(sampleRestService.healthCheckUrl OK as.String).futureValue
    assert(actualResponse === expectedResponse)
  }

}

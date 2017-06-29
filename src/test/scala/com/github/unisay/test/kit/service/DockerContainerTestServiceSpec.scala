package com.github.unisay.test.kit.service

import configs.syntax._
import com.typesafe.config.ConfigFactory
import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import com.github.unisay.test.kit.ScalatestServiceKit
import com.github.unisay.test.kit.service.ReadinessNotifier.{healthCheck, immediately}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scalaj.http._

/**
  * Sample docker container based test service
  */
class DockerContainerTestServiceSpec extends FeatureSpec with GivenWhenThen with MustMatchers with ScalatestServiceKit {

  val config = ConfigFactory.load()

  val container1Config = config.get[DockerContainerConfig]("sample-container-1").value
  val container1 = new DockerContainerTestService(container1Config, immediately) with SuiteLifecycle

  val container2Config = config.get[DockerContainerConfig]("sample-container-2").value
  val container2 = new DockerContainerTestService(container2Config,
    healthCheck(s"http://localhost:${container2Config.portBindings.head.external}/health")) with TestLifecycle

  override def testServices = container1 inParallelWith container2
  override val cancelSuiteOnTestServiceException = true

  scenario("Start sample docker container before and stop it after test suite") {

    Given("Sample docker containers expose resource via HTTP")
    val resourceUrl1 = s"http://localhost:${container1.portBindings.head.external}/resource.htm"
    val resourceUrl2 = s"http://localhost:${container2.portBindings.head.external}/resource.htm"

    When("Requests to the exposed resources are made")
    val response1 = Http(resourceUrl1).asString
    val response2 = Http(resourceUrl2).asString

    Then("Successful responses contain data from shared folder")
    val resourceContents = Source.fromFile("src/test/resources/shared_with_docker/resource.htm").mkString
    (response1.is2xx, response1.body) mustBe(true, resourceContents)
    (response2.is2xx, response2.body) mustBe(true, resourceContents)
  }

}

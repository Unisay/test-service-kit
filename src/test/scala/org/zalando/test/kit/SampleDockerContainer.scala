package org.zalando.test.kit

import org.zalando.test.kit.service.{DockerTestService, DockerTestServiceConfig}

case class SampleDockerContainerConfig(docker: DockerTestServiceConfig)

/**
  * Sample test service that represents docker container
  */
class SampleDockerContainer(val config: SampleDockerContainerConfig) extends DockerTestService(config.docker) {
  override def name = "Sample docker container"
  override protected def awaitContainer(): Unit = {}
}
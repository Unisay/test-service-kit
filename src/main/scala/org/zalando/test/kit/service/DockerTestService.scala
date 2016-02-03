package org.zalando.test.kit.service

import java.io.File
import java.net.{Inet4Address, NetworkInterface}

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model._
import com.github.dockerjava.core.command.AttachContainerResultCallback
import com.github.dockerjava.core.{DockerClientBuilder, DockerClientConfig}
import org.slf4j.LoggerFactory

import scala.sys.process.Process
import scala.util.Try

case class DockerApiConfig(socket: String, url: String)
case class SharedDirectoryConfig(internal: String, external: String)
case class DockerTestServiceConfig(api: DockerApiConfig,
                                   portBindings: Set[Int],
                                   shared: SharedDirectoryConfig,
                                   commandLineArguments: Seq[String])

abstract class DockerTestService(val dockerConfig: DockerTestServiceConfig) extends TestService {

  type ContainerId = String

  private val logger = LoggerFactory.getLogger(classOf[DockerTestService])

  private lazy val dockerClientConfig = DockerClientConfig.createDefaultConfigBuilder()
    .withUri(getDockerApiUri(dockerConfig.api.socket, dockerConfig.api.url))
    .build()

  private lazy val docker = DockerClientBuilder.getInstance(dockerClientConfig).build()

  protected val dockerHostIp = getDockerHostIp

  protected def awaitContainer(): Unit

  private lazy val container: Option[(ContainerId, ResultCallback[Frame])] = for {
    imageName <- findMostRecentImageName("article-service")
    containerId = startDockerContainer(imageName, s"dockerhost:$dockerHostIp", dockerConfig.portBindings)
    attachedStream = attachContainer(containerId)
  } yield (containerId, attachedStream)

  var shutdownProcess: Option[Process] = None

  override def beforeSuite(): Unit = {
    if (container.isEmpty)
      sys.error("At least one docker image has to be published locally. Use sbt docker:publishLocal")
  }

  private def startDockerContainer(imageName: String, extraHosts: String, ports: Set[Int]): ContainerId = {
    val portBindings = new Ports()
    ports.foreach { port ⇒
      portBindings.bind(ExposedPort.tcp(port), Ports.Binding(port))
    }

    val containerId = docker.createContainerCmd(imageName)
      .withExposedPorts(ports.map(ExposedPort.tcp).toSeq: _*)
      .withExtraHosts(extraHosts)
      .withPortBindings(portBindings)
      .withCmd(dockerConfig.commandLineArguments: _*)
      .withBinds(new Bind(
        new File(dockerConfig.shared.external).getAbsolutePath,
        new Volume(dockerConfig.shared.internal)))
      .exec()
      .getId

    docker.startContainerCmd(containerId).exec()
    logger.info("Starting docker container: {}", imageName)

    val inspectContainerResponse = docker.inspectContainerCmd(containerId).exec()
    logger.info("Docker container {} started == {}", containerId, inspectContainerResponse.getState.isRunning)

    containerId
  }

  private def getDockerHostIp: String = {
    val dockerHostIp = getNetworkInterfaceIp("docker0")
      .orElse(getNetworkInterfaceIp("vboxnet0"))
      .orElse(getNetworkInterfaceIp("vboxnet1"))
    if (dockerHostIp.isEmpty)
      sys.error("Failed to determine docker host IP by searching for a docker0, vboxnet0 or vboxnet1 network interface")
    else {
      logger.info("Detected docker host IP: {}", dockerHostIp.get)
      dockerHostIp.get
    }
  }

  private def getDockerApiUri(apiSocketPath: String, apiUrl: String): String =
    if (new File(apiSocketPath).exists)
      s"unix://$apiSocketPath"
    else
      apiUrl

  private def getNetworkInterfaceIp(networkInterfaceName: String): Option[String] = {
    import scala.collection.JavaConversions._
    NetworkInterface
      .getNetworkInterfaces.toList
      .filter(_.getDisplayName == networkInterfaceName)
      .flatMap(_.getInetAddresses)
      .filter(_.isInstanceOf[Inet4Address])
      .map(_.getHostAddress)
      .headOption
  }

  private def attachContainer(containerId: ContainerId): ResultCallback[Frame] = {
    val callback = new AttachContainerResultCallback {
      override def onNext(article: Frame): Unit = {
        logger.info("\n> {}", new String(article.getPayload).trim)
      }

      override def onError(throwable: Throwable): Unit = {
        logger.error("Error from docker container", throwable)
      }
    }

    val resultCallback = docker.attachContainerCmd(containerId)
      .withStdErr()
      .withStdOut()
      .withFollowStream()
      .withLogs()
      .exec(callback)

    awaitContainer()
    resultCallback
  }

  private def findMostRecentImageName(substring: String): Option[String] = {
    import scala.collection.JavaConversions._
    docker
      .listImagesCmd()
      .exec()
      .iterator()
      .toList
      .filter(_.getRepoTags.head.contains(substring))
      .sortWith((image1, image2) ⇒ image1.getCreated.compareTo(image2.getCreated) > 0)
      .flatMap(_.getRepoTags)
      .headOption
  }

  override def afterSuite(): Unit = {
    container.foreach { containerInfo ⇒
      val (id, attached) = containerInfo
      Try(attached.close())
      if (docker.inspectContainerCmd(id).exec().getState.isRunning) {
        logger.info("Stopping docker container: {}", id)
        docker.stopContainerCmd(id).exec()
        docker.waitContainerCmd(id).exec() // blocks until container is stopped
        logger.info("Docker container {} stopped", id)
      } else {
        logger.info("Container {} is not running", id)
      }
    }
  }

}

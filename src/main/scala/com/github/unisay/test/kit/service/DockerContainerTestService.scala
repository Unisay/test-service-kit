package com.github.unisay.test.kit.service

import java.io.File
import java.net.{Inet4Address, NetworkInterface}

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model._
import com.github.dockerjava.core.command.AttachContainerResultCallback
import com.github.dockerjava.core.{DockerClientBuilder, DockerClientConfig}
import com.typesafe.scalalogging.StrictLogging
import com.github.unisay.test.kit.TestServiceException
import com.github.unisay.test.kit.service.ReadinessNotifier.immediately
import scala.collection.JavaConverters._

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

case class HealthCheckConfig(url: String, timeout: FiniteDuration)

case class PortBindingConfig(internal: Int, external: Int)

case class SharedFolderConfig(internal: String, external: String)

case class DockerContainerConfig(imageNameSubstring: String,
                                 dockerApiUri: String,
                                 serviceName: Option[String] = None,
                                 portBindings: Set[PortBindingConfig] = Set.empty,
                                 sharedFolders: Set[SharedFolderConfig] = Set.empty,
                                 commandLineArguments: Seq[String] = Seq.empty)

class DockerContainerTestService(override val name: String,
                                 val imageNameSubstring: String,
                                 val dockerApiUri: String,
                                 val portBindings: Set[PortBindingConfig] = Set.empty,
                                 val sharedFolders: Set[SharedFolderConfig] = Set.empty,
                                 val commandLineArguments: Seq[String] = Seq.empty,
                                 val readinessNotifier: ReadinessNotifier = immediately)
  extends TestService with StrictLogging {

  def this(config: DockerContainerConfig, readinessNotifier: ReadinessNotifier) = this(
    config.serviceName.getOrElse(s"Docker container ${config.imageNameSubstring}"),
    config.imageNameSubstring,
    config.dockerApiUri,
    config.portBindings,
    config.sharedFolders,
    config.commandLineArguments,
    readinessNotifier)

  type ContainerId = String

  private var state: Try[(DockerClient, ContainerId, ResultCallback[Frame])] =
    Failure(new RuntimeException("not initialized"))

  def start(): Unit = {
    state = for {
      client ← createDockerClient
      imageName <- findMostRecentImageName(client, imageNameSubstring)
      containerId ← startDockerContainer(client, imageName, s"dockerhost:$dockerHostIp", portBindings, sharedFolders)
      attachedStream ← attachContainer(client, containerId)
    } yield (client, containerId, attachedStream)

    state.failed.foreach(throw _)
  }

  def reset(): Unit = {}

  private def createDockerClient: Try[DockerClient] = Try {
    DockerClientBuilder.getInstance(DockerClientConfig.createDefaultConfigBuilder().withUri(dockerApiUri).build()).build()
  }

  private def startDockerContainer(client: DockerClient,
                                   imageName: String,
                                   extraHosts: String,
                                   portBindings: Set[PortBindingConfig],
                                   sharedFolders: Set[SharedFolderConfig]): Try[ContainerId] = Try {

    val containerId = client.createContainerCmd(imageName)
      .withExposedPorts(portBindings.map(binding => ExposedPort.tcp(binding.internal)).toSeq: _*)
      .withExtraHosts(extraHosts)
      .withPortBindings {
        val bindings = new Ports()
        portBindings.foreach { binding =>
          bindings.bind(ExposedPort.tcp(binding.internal), Ports.Binding(binding.external))
        }
        bindings
      }
      .withCmd(commandLineArguments: _*)
      .withBinds(
        sharedFolders.map { sharedFolderConfig =>
          new Bind(makeAbsolutePath(sharedFolderConfig.external), new Volume(sharedFolderConfig.internal))
        }.toSeq: _*
      )
      .exec()
      .getId

    logger.info(s"Starting docker container: $imageName")
    client.startContainerCmd(containerId).exec()

    assert(client.inspectContainerCmd(containerId).exec().getState.isRunning)
    logger.info(s"Docker container $containerId started")

    containerId
  }

  protected def makeAbsolutePath(absoluteOrRelativePath: String): String = {
    if (new File(absoluteOrRelativePath).exists()) {
      absoluteOrRelativePath
    } else {
      val maybeFile = Option(getClass.getResource(absoluteOrRelativePath)).map(_.getFile)
      if (maybeFile.isEmpty)
        throw TestServiceException(s"Failed to find absolute path for: $absoluteOrRelativePath")
      maybeFile.get
    }
  }

  private def dockerHostIp: String =
    interfaceIp("docker0").orElse(interfaceIp("vboxnet0")).orElse(interfaceIp("vboxnet1")) match {
      case Some(ip) =>
        logger.info(
          s"Detected docker host IP ($ip). It will be reachable from inside the container by domain name 'dockerhost'")
        ip
      case _ =>
        throw TestServiceException(
          "Failed to determine docker host IP by searching for a docker0, vboxnet0 or vboxnet1 network interface")
    }

  private def interfaceIp(networkInterfaceName: String): Option[String] = {
    NetworkInterface
      .getNetworkInterfaces.asScala.toList
      .filter(_.getDisplayName == networkInterfaceName)
      .flatMap(_.getInetAddresses.asScala.toList)
      .filter(_.isInstanceOf[Inet4Address])
      .map(_.getHostAddress)
      .headOption
  }

  private def attachContainer(client: DockerClient, containerId: ContainerId): Try[ResultCallback[Frame]] = Try {
    val callback = new AttachContainerResultCallback {
      override def onNext(frame: Frame): Unit = {
        logger.info("\n> {}", new String(frame.getPayload).trim)
      }

      override def onError(throwable: Throwable): Unit = {
        logger.error("Error from docker container", throwable)
      }
    }

    val resultCallback = client.attachContainerCmd(containerId)
      .withStdErr()
      .withStdOut()
      .withFollowStream()
      .withLogs()
      .exec(callback)

    readinessNotifier.awaitReady()
    resultCallback
  }

  private def findMostRecentImageName(client: DockerClient, substring: String): Try[String] = {
    client
      .listImagesCmd()
      .exec()
      .iterator()
      .asScala
      .toList
      .filter(_.getRepoTags.head.contains(substring))
      .sortWith((image1, image2) => image1.getCreated.compareTo(image2.getCreated) > 0)
      .flatMap(_.getRepoTags)
      .headOption match {
      case Some(string) =>
        Success(string)
      case None =>
        Failure(throw TestServiceException(
          s"At least one docker image ($imageNameSubstring) has to be published locally"))
    }
  }

  def stop(): Unit = state foreach {
    case (client, id, attached) =>
      Try(attached.close())
      if (client.inspectContainerCmd(id).exec().getState.isRunning) {
        logger.info(s"Stopping docker container: $id")
        client.stopContainerCmd(id).exec()
        client.waitContainerCmd(id).exec() // blocks until container is stopped
        logger.info(s"Docker container $id stopped")
      } else {
        logger.info(s"Container $id is not running")
      }
  }


}

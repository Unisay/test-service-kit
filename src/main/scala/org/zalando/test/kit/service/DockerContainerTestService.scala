package org.zalando.test.kit.service

import java.io.File
import java.net.{Inet4Address, NetworkInterface}

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model._
import com.github.dockerjava.core.command.AttachContainerResultCallback
import com.github.dockerjava.core.{DockerClientBuilder, DockerClientConfig}
import com.typesafe.scalalogging.StrictLogging
import org.zalando.test.kit.TestServiceException
import org.zalando.test.kit.service.DockerContainerTestService.ready

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, Future}
import scala.util.Try

case class HealthCheckConfig(url: String, timeout: FiniteDuration)

case class PortBindingConfig(internal: Int, external: Int)

case class SharedFolderConfig(internal: String, external: String)

case class DockerContainerConfig(imageNameSubstring: String,
                                 apiUri: String,
                                 serviceName: Option[String] = None,
                                 portBindings: Set[PortBindingConfig] = Set.empty,
                                 sharedFolders: Set[SharedFolderConfig] = Set.empty,
                                 commandLineArguments: Seq[String] = Seq.empty)

object DockerContainerTestService {
  implicit val ready: (DockerContainerTestService) ⇒ Future[Unit] = _ ⇒ Future.successful(())
}

class DockerContainerTestService(override val name: String,
                                 val imageNameSubstring: String,
                                 val apiUri: String,
                                 val portBindings: Set[PortBindingConfig] = Set.empty,
                                 val sharedFolders: Set[SharedFolderConfig] = Set.empty,
                                 val commandLineArguments: Seq[String] = Seq.empty)
                                (implicit val readinessChecker: (DockerContainerTestService) ⇒ Future[Unit] = ready)
  extends TestService with SuiteLifecycle with StrictLogging {

  def this(config: DockerContainerConfig) = this(
    config.serviceName.getOrElse(s"Docker container ${config.imageNameSubstring}"),
    config.imageNameSubstring,
    config.apiUri,
    config.portBindings,
    config.sharedFolders,
    config.commandLineArguments)

  type ContainerId = String

  private lazy val dockerClientConfig = DockerClientConfig.createDefaultConfigBuilder()
    .withUri(if (new File(apiUri).exists) s"unix://$apiUri" else apiUri)
    .build()

  private var state: Option[(DockerClient, ContainerId, ResultCallback[Frame])] = None

  override def start(): Unit = {
    state = for {
      client ← Option(DockerClientBuilder.getInstance(dockerClientConfig).build())
      imageName <- findMostRecentImageName(client, imageNameSubstring)
      containerId = startDockerContainer(client, imageName, s"dockerhost:$dockerHostIp", portBindings, sharedFolders)
      attachedStream = attachContainer(client, containerId)
    } yield (client, containerId, attachedStream)

    if (state.isEmpty)
      throw new TestServiceException(s"At least one docker image ($imageNameSubstring) has to be published locally")
  }

  private def startDockerContainer(client: DockerClient,
                                   imageName: String,
                                   extraHosts: String,
                                   portBindings: Set[PortBindingConfig],
                                   sharedFolders: Set[SharedFolderConfig]): ContainerId = {

    val containerId = client.createContainerCmd(imageName)
      .withExposedPorts(portBindings.map(binding ⇒ ExposedPort.tcp(binding.internal)).toSeq: _*)
      .withExtraHosts(extraHosts)
      .withPortBindings {
        val bindings = new Ports()
        portBindings.foreach { binding ⇒
          bindings.bind(ExposedPort.tcp(binding.internal), Ports.Binding(binding.external))
        }
        bindings
      }
      .withCmd(commandLineArguments: _*)
      .withBinds(
        sharedFolders.map { sharedFolderConfig ⇒
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
        throw new TestServiceException(s"Failed to find absolute path for: $absoluteOrRelativePath")
      maybeFile.get
    }
  }

  private def dockerHostIp: String =
    interfaceIp("docker0").orElse(interfaceIp("vboxnet0")).orElse(interfaceIp("vboxnet1")) match {
      case Some(ip) ⇒
        logger.info(
          s"Detected docker host IP ($ip). It will be reachable from inside the container by domain name 'dockerhost'")
        ip
      case _ ⇒
        throw new TestServiceException(
          "Failed to determine docker host IP by searching for a docker0, vboxnet0 or vboxnet1 network interface")
    }

  private def interfaceIp(networkInterfaceName: String): Option[String] = {
    import scala.collection.JavaConversions._
    NetworkInterface
      .getNetworkInterfaces.toList
      .filter(_.getDisplayName == networkInterfaceName)
      .flatMap(_.getInetAddresses)
      .filter(_.isInstanceOf[Inet4Address])
      .map(_.getHostAddress)
      .headOption
  }

  private def attachContainer(client: DockerClient, containerId: ContainerId): ResultCallback[Frame] = {
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

    Await.ready(readinessChecker(this), Duration.Inf)
    resultCallback
  }

  private def findMostRecentImageName(client: DockerClient, substring: String): Option[String] = {
    import scala.collection.JavaConversions._
    client
      .listImagesCmd()
      .exec()
      .iterator()
      .toList
      .filter(_.getRepoTags.head.contains(substring))
      .sortWith((image1, image2) ⇒ image1.getCreated.compareTo(image2.getCreated) > 0)
      .flatMap(_.getRepoTags)
      .headOption
  }

  override def stop(): Unit = state foreach {
    case (client, id, attached) ⇒
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

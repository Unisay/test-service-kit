package com.github.unisay.test.kit.service

import java.io._
import java.util.function.Consumer

import com.typesafe.scalalogging.StrictLogging
import scala.collection.JavaConverters._

class JvmTestService(override val name: String,
                     mainClass: String,
                     jvmArgs: List[String] = Nil,
                     programArgs: List[String] = Nil,
                     customClassPath: Option[String] = None,
                     output: PrintStream = System.out,
                     forceStop: Boolean = false) extends TestService with StrictLogging {

  lazy val maybeProcessBuilder = for {
    fileSeparator ← sys.props.get("file.separator")
    pathSeparator ← sys.props.get("path.separator")
    javaHome ← sys.props.get("java.home")
    javaPath = javaHome + fileSeparator + "bin" + fileSeparator + "java"
    cp ← sys.props.get("java.class.path").flatMap(path => customClassPath.map(ccp => path + pathSeparator + ccp))

  } yield {
    val arguments = (javaPath :: jvmArgs) ++ ("-cp" :: cp :: mainClass :: programArgs)
    new ProcessBuilder(arguments: _*)
  }

  var maybeProcess: Option[Process] = None

  def start() = {
    maybeProcess = maybeProcessBuilder.map { builder =>
      logger.info("Starting process: " + builder.command().asScala.mkString(" "))
      val process = builder.start()
      new StreamGobbler(process.getInputStream, output).start()
      new StreamGobbler(process.getErrorStream, output).start()
      process
    }
    ()
  }

  def reset(): Unit = {}

  def stop() = {
    maybeProcess.foreach { process =>
      if (process.isAlive) {
        if (forceStop) {
          logger.info("Forcibly destroying process...")
          process.destroyForcibly().waitFor()
        } else {
          logger.info("Destroying process...")
          process.destroy()
        }
      }
    }
  }

  class StreamGobbler(val inputStream: InputStream, val printStream: PrintStream) extends Thread {
    setDaemon(true)
    override def run(): Unit = {
      val reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))
      reader.lines().forEach(new Consumer[String] {
        def accept(line: String) = printStream.println(line)
      })
    }
  }
}

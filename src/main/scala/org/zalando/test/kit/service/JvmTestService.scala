package org.zalando.test.kit.service

import java.io._
import java.util.function.Consumer

import com.typesafe.scalalogging.StrictLogging
import scala.collection.JavaConversions._

class JvmTestService(override val name: String,
                     mainClass: String,
                     args: List[String] = Nil,
                     customClassPath: Option[String] = None,
                     output: PrintStream = System.out,
                     forceStop: Boolean = false) extends TestService with StrictLogging {

  lazy val maybeProcessBuilder = for {
    fsep ← sys.props.get("file.separator")
    psep ← sys.props.get("path.separator")
    javaHome ← sys.props.get("java.home")
    javaPath = javaHome + fsep + "bin" + fsep + "java"
    cp ← sys.props.get("java.class.path").flatMap(path ⇒ customClassPath.map(ccp ⇒ path + psep + ccp))
  } yield new ProcessBuilder((javaPath :: "-cp" :: cp :: mainClass :: args).toArray:_*)

  var maybeProcess: Option[Process] = None

  def start() = {
    maybeProcess = maybeProcessBuilder.map { builder ⇒
      logger.info("Starting process: " + builder.command().mkString(" "))
      val process = builder.start()
      new StreamGobbler(process.getInputStream, output).start()
      new StreamGobbler(process.getErrorStream, output).start()
      process
    }
    ()
  }

  def reset(): Unit = {}

  def stop() = {
    maybeProcess.foreach { process ⇒
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
    override def run(): Unit = {
      val reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))
      reader.lines().forEach(new Consumer[String] {
        def accept(line: String) = printStream.println(line)
      })
    }
  }
}

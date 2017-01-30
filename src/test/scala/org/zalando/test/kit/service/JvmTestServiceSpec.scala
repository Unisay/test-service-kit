package org.zalando.test.kit.service

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets.UTF_8

import org.scalatest.{FlatSpec, MustMatchers}
import org.zalando.test.kit.ScalatestServiceKit
import org.zalando.test.kit.service.TestService.Composition

class JvmTestServiceSpec extends FlatSpec with MustMatchers with ScalatestServiceKit {

  private val outputStream = new ByteArrayOutputStream()
  private val printStream = new PrintStream(outputStream)
  private val scalaVersion = if (sys.props.getOrElse("version.number", "unknown").startsWith("2.12")) "2.12" else "2.11"

  private val testService = new JvmTestService(
    name = "JVM Test Service",
    mainClass = TestApplication.getClass.getName.stripSuffix("$"),
    jvmArgs = List("-Xms64m", "-Xmx256m"),
    programArgs = List("Hello", "World"),
    customClassPath = Some(s"target/scala-$scalaVersion/test-classes"),
    output = printStream
  ) with SuiteLifecycle

  def testServices: Composition = testService

  behavior of "JvmTestService"

  it must "start and stop" in {
    Thread.sleep(2000)
    new String(outputStream.toByteArray, UTF_8) must startWith {
      "Test Application started with arguments: Hello World\nWorking..."
    }
  }
}

package org.zalando.test.kit.service

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets.UTF_8

import org.scalatest.{FlatSpec, MustMatchers}
import org.zalando.test.kit.ScalatestServiceKit
import org.zalando.test.kit.service.TestService.Composition

class JvmTestServiceSpec extends FlatSpec with MustMatchers with ScalatestServiceKit {

  val outputStream = new ByteArrayOutputStream()
  val printStream = new PrintStream(outputStream)
  val testService = new JvmTestService(
    name = "JVM Test Service",
    mainClass = TestApplication.getClass.getName.stripSuffix("$"),
    args = List("Hello", "World"),
    customClassPath = Some("target/scala-2.11/test-classes"),
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

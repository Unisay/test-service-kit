package org.zalando.test.kit.service

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets.UTF_8

import org.scalatest.{FlatSpec, MustMatchers}

class JvmTestServiceSpec extends FlatSpec with MustMatchers {

  val outputStream = new ByteArrayOutputStream()
  val printStream = new PrintStream(outputStream)
  val testService = new JvmTestService(
    name = "JVM Test Service",
    mainClass = TestApplication.getClass.getName.stripSuffix("$"),
    args = List("Hello", "World"),
    customClassPath = Some("target/scala-2.11/test-classes"),
    output = printStream
  )

  behavior of "JvmTestService"

  it must "start and stop" in {
    try {
      testService.start()
      Thread.sleep(2000)
    } finally {
      testService.stop()
    }

    new String(outputStream.toByteArray, UTF_8) must startWith {
      "Test Application started with arguments: Hello World\nWorking..."
    }
  }

}

package com.github.unisay.test.kit

import scala.io.Source

object UnsafeIo {
  def readClasspathResource(name: String): String = Source.fromInputStream(getClass.getResourceAsStream(name)).mkString
  def readFileResource(path: String): String = Source.fromFile(path, "utf8").mkString
}

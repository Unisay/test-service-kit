package org.zalando.test.kit.service

trait TestLifecycle extends TestService {

  abstract override def beforeTest(): Unit = {
    start()
    super.beforeTest()
  }

  abstract override def afterTest(): Unit = {
    super.afterTest()
    stop()
  }

  def start(): Unit

  def stop(): Unit
}

package org.zalando.test.kit.service

trait TestLifecycle extends TestService {

  override def beforeTest(): Unit = {
    start()
    super.beforeTest()
  }

  override def afterTest(): Unit = {
    super.afterTest()
    stop()
  }

  def start(): Unit

  def stop(): Unit
}

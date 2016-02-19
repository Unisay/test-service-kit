package org.zalando.test.kit.service

trait TestLifecycle {
  this: TestService ⇒

  override def beforeTest(): Unit = start()

  override def afterTest(): Unit = stop()

  def start(): Unit

  def stop(): Unit
}

package org.zalando.test.kit.service

trait SuiteLifecycle {
  this: TestService ⇒

  override def beforeSuite(): Unit = start()

  override def afterSuite(): Unit = stop()

  def start(): Unit

  def stop(): Unit
}

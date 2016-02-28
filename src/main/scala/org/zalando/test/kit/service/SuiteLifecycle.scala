package org.zalando.test.kit.service

trait SuiteLifecycle extends TestService {

  override def beforeSuite(): Unit = {
    start()
    super.beforeSuite()
  }

  override def afterSuite(): Unit = {
    super.afterSuite()
    stop()
  }

  def start(): Unit

  def stop(): Unit
}

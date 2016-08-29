package org.zalando.test.kit.service

trait SuiteLifecycle extends TestService {

  abstract override def beforeSuite(): Unit = {
    start()
    super.beforeSuite()
  }

  abstract override def beforeTest(): Unit = {
    reset()
    super.beforeTest()
  }

  abstract override def afterSuite(): Unit = {
    super.afterSuite()
    stop()
  }

  def start(): Unit
  def reset(): Unit
  def stop(): Unit
}

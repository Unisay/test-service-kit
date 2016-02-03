package org.zalando.test.kit

import org.zalando.test.kit.service.TestService

trait TestServiceKit {

  def testServices: List[TestService]

  def beforeSuite(): Unit =
    testServices.foreach(service ⇒ handleExceptions(service)(_.beforeSuite()))

  def beforeTest(): Unit =
    testServices.foreach(service ⇒ handleExceptions(service)(_.beforeTest()))

  def afterTest(): Unit =
    testServices.foreach(service ⇒ handleExceptions(service)(_.afterTest()))

  def afterSuite(): Unit =
    testServices.reverse.foreach(service ⇒ handleExceptions(service)(_.afterSuite()))

  protected def handleExceptions(testService: TestService)(service: TestService ⇒ Unit): Unit
}

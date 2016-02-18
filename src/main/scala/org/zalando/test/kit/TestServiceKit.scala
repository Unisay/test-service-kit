package org.zalando.test.kit

import com.typesafe.scalalogging.StrictLogging
import org.zalando.test.kit.service.TestService
import org.zalando.test.kit.service.TestService.Composition

trait TestServiceKit extends StrictLogging {

  def testServices: Composition

  def beforeSuite(): Unit =
    testServices.visitInOrder {
      service ⇒ handleExceptions(service) {
        logger.trace(s"Visiting ${service.name} before suite")
        _.beforeSuite()
      }
    }

  def beforeTest(): Unit =
    testServices.visitInOrder {
      service ⇒ handleExceptions(service) {
        logger.trace(s"Visiting ${service.name} before test")
        _.beforeTest()
      }
    }

  def afterTest(): Unit =
    testServices.visitInReverseOrder {
      service ⇒ handleExceptions(service) {
        logger.trace(s"Visiting ${service.name} after test")
        _.afterTest()
      }
    }

  def afterSuite(): Unit =
    testServices.visitInReverseOrder {
      service ⇒ handleExceptions(service) {
        logger.trace(s"Visiting ${service.name} after suite")
        _.afterSuite()
      }
    }

  protected def handleExceptions(testService: TestService)(service: TestService ⇒ Unit): Unit
}

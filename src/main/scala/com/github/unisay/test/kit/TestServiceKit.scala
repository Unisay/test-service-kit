package com.github.unisay.test.kit

import com.typesafe.scalalogging.StrictLogging
import com.github.unisay.test.kit.service.TestService.Composition
import com.github.unisay.test.kit.service.TestService.Composition

case class TestServiceException(message: String) extends RuntimeException(message)

trait TestServiceKit extends StrictLogging {

  def testServices: Composition

  def beforeSuite(): Unit = testServices.visitInOrder { service =>
    logger.trace(s"Visiting ${service.name} before suite")
    service.beforeSuite()
  }

  def beforeTest(): Unit = testServices.visitInOrder { service =>
    logger.trace(s"Visiting ${service.name} before test")
    service.beforeTest()
  }

  def afterTest(): Unit = testServices.visitInReverseOrder { service =>
    logger.trace(s"Visiting ${service.name} after test")
    service.afterTest()
  }

  def afterSuite(): Unit = testServices.visitInReverseOrder { service =>
    logger.trace(s"Visiting ${service.name} after suite")
    service.afterSuite()
  }

}

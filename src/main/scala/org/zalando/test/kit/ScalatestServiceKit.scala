package org.zalando.test.kit

import org.scalatest._
import org.zalando.test.kit.service.TestService

import scala.util.Try

trait ScalatestServiceKit extends BeforeAndAfterAll with TestServiceKit {
  this: Suite ⇒

  override abstract def withFixture(test: NoArgTest): Outcome = {
    beforeTest()
    val outcome = super.withFixture(test)
    afterTest()
    outcome
  }

  override protected def beforeAll(): Unit = beforeSuite()

  override protected def afterAll(): Unit = afterSuite()

  protected override def handleExceptions(testService: TestService)(doWithService: TestService ⇒ Unit): Unit =
    Try(doWithService(testService)).failed.foreach { throwable ⇒
      throwable.printStackTrace() //TODO: should use logger
      throw new RuntimeException(s"${testService.name} failed: " + throwable.getMessage, throwable)
    }

}

package org.zalando.test.kit

import org.scalatest._

import scala.util.{Success, Try}

trait ScalatestServiceKit extends BeforeAndAfterAll with TestServiceKit {
  this: Suite ⇒

  val cancelSuiteOnTestServiceException = false

  @volatile var suiteOutcome: Option[Outcome] = None

  override abstract def withFixture(test: NoArgTest): Outcome =
    suiteOutcome match {
      case Some(outcome) ⇒
        outcome
      case None ⇒
        beforeTest()
        val testOutcome = super.withFixture(test)
        val afterOutcome = Try(afterTest()).transform(
          _ ⇒ Success[Outcome](Succeeded),
          t ⇒ Success[Outcome](Failed(t))
        ).get
        if (testOutcome.isSucceeded && afterOutcome.isFailed)
          afterOutcome
        else
          testOutcome
    }

  override protected def beforeAll(): Unit = {
    Try(beforeSuite()).recover {
      case e if cancelSuiteOnTestServiceException ⇒
        suiteOutcome = Some(Canceled(e.getMessage, e))
      case e ⇒
        suiteOutcome = Some(Failed(e.getMessage, e))
    }
  }

  override protected def afterAll(): Unit = afterSuite()
}

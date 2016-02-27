package org.zalando.test.kit

import org.specs2.execute._
import org.specs2.specification._
import org.specs2.specification.dsl.mutable.ActionDsl

trait Specs2ServiceKit extends BeforeAfterAll with AroundEach with TestServiceKit with ActionDsl {
  stopWhen(Error())

  def around[R: AsResult](r: => R): Result =
    BeforeAfter.create(beforeTest(), afterTest())(r)

  def beforeAll = ResultExecution.effectively {
    beforeSuite()
    Success()
  }

  def afterAll = ResultExecution.effectively {
    afterSuite()
    Success()
  }

}

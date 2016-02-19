package org.zalando.test.kit.service

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.implicitConversions

trait TestService {
  def name: String
  def beforeSuite(): Unit = {}
  def beforeTest(): Unit = {}
  def afterTest(): Unit = {}
  def afterSuite(): Unit = {}
}

object TestService {

  sealed trait Composition {
    def inParallelWith(right: ⇒ Composition)(implicit ec: ExecutionContext): Composition = ||(right)
    def ||(right: ⇒ Composition)(implicit ec: ExecutionContext): Composition = new Parallel(this, right)
    def andThen(right: ⇒ Composition): Composition = >>(right)
    def >>(right: ⇒ Composition): Composition = new Sequential(this, right)
    def visitInOrder(visitor: TestService ⇒ Unit): Unit
    def visitInReverseOrder(visitor: TestService ⇒ Unit): Unit = visitInOrder(visitor)
  }

  implicit class UnitComposition(val left: TestService) extends Composition {
    override def visitInOrder(visitor: (TestService) ⇒ Unit): Unit = visitor(left)
  }

  class Parallel(left: ⇒ Composition, right: ⇒ Composition)(implicit ec: ExecutionContext) extends Composition {
    override def visitInOrder(visitor: (TestService) ⇒ Unit): Unit = {
      val leftVisit = Future(left.visitInOrder(visitor))
      val rightVisit = Future(right.visitInOrder(visitor))
      val both = Future.sequence(Set(leftVisit, rightVisit))
      Await.result(both, Duration.Inf)
    }
  }

  class Sequential(left: ⇒ Composition, right: ⇒ Composition) extends Composition {
    override def visitInOrder(visitor: (TestService) ⇒ Unit): Unit = {
      left.visitInOrder(visitor)
      right.visitInOrder(visitor)
    }
    override def visitInReverseOrder(visitor: (TestService) ⇒ Unit): Unit = {
      right.visitInReverseOrder(visitor)
      left.visitInReverseOrder(visitor)
    }
  }

}

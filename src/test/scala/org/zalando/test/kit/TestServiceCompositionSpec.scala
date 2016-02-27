package org.zalando.test.kit

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import org.zalando.test.kit.service.TestService

import scala.concurrent.ExecutionContext.Implicits.global

class TestServiceCompositionSpec
  extends FeatureSpec
    with GivenWhenThen
    with MustMatchers
    with ScalatestServiceKit
    with StrictLogging {

  val colorService = new ColorService
  val materialService = new MaterialService
  lazy val factoryService = new FactoryService(colorService.color, materialService.material)
  lazy val clientService = new FactoryClientService

  override def testServices = (colorService inParallelWith materialService) andThen factoryService andThen clientService
//  override def testServices = (colorService || materialService) >> factoryService >> clientService

  scenario("test services are started and stopped in order") {
    /*
      Debug output must be:

      Color created
      Material created

      Material before suite
      Color before suite
      Factory(Red, Metal) created
      Factory(Red, Metal) before suite
      Factory client created
      Factory client before suite

      Color before test
      Material before test
      Factory(Red, Metal) before test
      Factory client before test

      Factory client after test
      Factory(Red, Metal) after test
      Material after test
      Color after test

      Factory client after suite
      Factory(Red, Metal) after suite
      Color after suite
      Material after suite

      Note that lazily declared test services are created
      immediately before first lifecycle method is called (beforeSuite),
      which is called in the order of sequential composition (>>)
     */
  }

}


object Color extends Enumeration {
  type Color = Value
  val Red, Green, Blue = Value
}

object Material extends Enumeration {
  type Material = Value
  val Metal, Plastic = Value
}

import Color._
import Material._

trait EmulatedTestService extends TestService with StrictLogging {
  var isStarted = false

  logger.info(s"$name created")
  override def beforeSuite(): Unit = {
    Thread.sleep(100)
    isStarted = true
    logger.info(s"$name before suite")
  }
  override def beforeTest(): Unit = {
    Thread.sleep(100)
    logger.info(s"$name before test")
  }
  override def afterTest(): Unit = {
    Thread.sleep(100)
    logger.info(s"$name after test")
  }
  override def afterSuite(): Unit = {
    Thread.sleep(100)
    isStarted = false
    logger.info(s"$name after suite")
  }
}

class ColorService extends EmulatedTestService {
  override def name = "Color"
  def color = {
    assert(isStarted)
    Color.values.iterator.next()
  }
}

class MaterialService extends EmulatedTestService {
  override def name = "Material"
  def material = {
    assert(isStarted)
    Material.values.iterator.next()
  }
}

class FactoryService(val color: Color, val material: Material) extends EmulatedTestService {
  override def name = s"Factory($color, $material)"
}

class FactoryClientService(override val name: String = "Factory client") extends EmulatedTestService

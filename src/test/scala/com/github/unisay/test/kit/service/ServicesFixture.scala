package com.github.unisay.test.kit.service

import com.typesafe.scalalogging.StrictLogging

/**
  * Created by ylazaryev on 27.02.16.
  */
trait ServicesFixture {

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
}

package com.github.unisay.test.kit.service

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import com.github.unisay.test.kit.ScalatestServiceKit
import scala.concurrent.ExecutionContext.Implicits.global

class TestServiceCompositionSpec
  extends FeatureSpec
    with GivenWhenThen
    with MustMatchers
    with ScalatestServiceKit
    with StrictLogging
    with ServicesFixture {

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


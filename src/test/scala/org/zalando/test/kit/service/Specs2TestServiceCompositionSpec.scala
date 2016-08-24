package org.zalando.test.kit.service

import org.specs2.mutable._
import org.zalando.test.kit.Specs2ServiceKit
import scala.concurrent.ExecutionContext.Implicits.global

class Specs2TestServiceCompositionSpec extends Specification with Specs2ServiceKit with ServicesFixture {

  lazy val colorService = new ColorService
  lazy val materialService = new MaterialService
  lazy val factoryService = new FactoryService(colorService.color, materialService.material)
  lazy val clientService = new FactoryClientService

  override def testServices = (colorService inParallelWith materialService) andThen factoryService andThen clientService

  "test services should be started and stopped in order" >> {
    /*
     * Expected output like:

     * Color created
     * Material created

     * Color before suite
     * Material before suite
     * Factory(Red, Metal) created
     * Factory(Red, Metal) before suite
     * Factory client created
     * Factory client before suite

     * Material before test
     * Color before test
     * Factory(Red, Metal) before test
     * Factory client before test

     * Factory client after test
     * Factory(Red, Metal) after test
     * Color after test
     * Material after test

     * Factory client after suite
     * Factory(Red, Metal) after suite
     * Material after suite
     * Color after suite
     */
    ok
  }

}

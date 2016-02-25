package org.zalando.test.kit

import org.specs2.execute.Error
import org.specs2.mutable._

import scala.concurrent.ExecutionContext.Implicits.global

class Specs2TestServiceCompositionSpec extends Specification with Specs2ServiceKit {

  lazy val colorService = new ColorService
  lazy val materialService = new MaterialService
  lazy val factoryService = new FactoryService(colorService.color, materialService.material)
  lazy val clientService = new FactoryClientService

  override def testServices = (colorService inParallelWith materialService) andThen factoryService andThen clientService

  "test services should be started and stopped in order" >> {
    /**

     *Expected output like:

*2016-02-25 14:25:41 org.zalando.test.kit.ColorService  INFO     | Color created
*2016-02-25 14:25:41 org.zalando.test.kit.MaterialService  INFO  | Material created

*2016-02-25 14:25:41 org.zalando.test.kit.ColorService  INFO     | Color before suite
*2016-02-25 14:25:41 org.zalando.test.kit.MaterialService  INFO  | Material before suite
*2016-02-25 14:25:41 org.zalando.test.kit.FactoryService  INFO   | Factory(Red, Metal) created
*2016-02-25 14:25:41 org.zalando.test.kit.FactoryService  INFO   | Factory(Red, Metal) before suite
*2016-02-25 14:25:41 o.z.test.kit.FactoryClientService  INFO     | Factory client created
*2016-02-25 14:25:41 o.z.test.kit.FactoryClientService  INFO     | Factory client before suite

*2016-02-25 14:25:41 org.zalando.test.kit.MaterialService  INFO  | Material before test
*2016-02-25 14:25:41 org.zalando.test.kit.ColorService  INFO     | Color before test
*2016-02-25 14:25:41 org.zalando.test.kit.FactoryService  INFO   | Factory(Red, Metal) before test
*2016-02-25 14:25:41 o.z.test.kit.FactoryClientService  INFO     | Factory client before test

*2016-02-25 14:25:42 o.z.test.kit.FactoryClientService  INFO     | Factory client after test
*2016-02-25 14:25:42 org.zalando.test.kit.FactoryService  INFO   | Factory(Red, Metal) after test
*2016-02-25 14:25:42 org.zalando.test.kit.ColorService  INFO     | Color after test
*2016-02-25 14:25:42 org.zalando.test.kit.MaterialService  INFO  | Material after test

*2016-02-25 14:25:42 o.z.test.kit.FactoryClientService  INFO     | Factory client after suite
*2016-02-25 14:25:42 org.zalando.test.kit.FactoryService  INFO   | Factory(Red, Metal) after suite
*2016-02-25 14:25:42 org.zalando.test.kit.MaterialService  INFO  | Material after suite
*2016-02-25 14:25:42 org.zalando.test.kit.ColorService  INFO     | Color after suite

     */
    ok
  }

}

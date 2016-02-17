package org.zalando.test.kit

import dispatch.url
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.zalando.test.kit.service.MockServerTestService
/**
  * Sample REST service mock
  */
class SampleRestService(override val mockServerPort: Int = 8080) extends MockServerTestService(mockServerPort) {
  override def name = "Sample REST service mock"

  val healthCheckUrl = url(s"http://localhost:$mockServerPort/health")

  def healthCheckRespondsWith(body: String): Unit =
    mockServer
      .when(request.withPath("/health"))
      .respond(response.withBody(body))

}

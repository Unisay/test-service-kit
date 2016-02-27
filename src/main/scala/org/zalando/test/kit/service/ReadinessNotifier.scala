package org.zalando.test.kit.service

import java.util.concurrent._

import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scalaj.http.{Http ⇒ Get}

sealed trait Ready
case object Ready extends Ready

trait ReadinessNotifier {

  protected val defaultTimeout: Duration = Duration.Inf
  protected def timeOut(): Unit = {}
  protected def timeOutMessage(duration: Duration) = s"Resource is not ready within duration ($duration)"
  def whenReady(): Future[Ready]
  def awaitReady(atMost: Duration = defaultTimeout): Ready = {
    try {
      Await.result(whenReady(), atMost)
    } catch {
      case e: TimeoutException ⇒
        timeOut()
        throw new RuntimeException(timeOutMessage(atMost), e)
    }
  }
}

object ReadinessNotifier {

  def immediately = new ReadinessNotifier {
    override def whenReady(): Future[Ready] = Future.successful(Ready)
  }

  def duration[R](duration: Duration) = new ReadinessNotifier {
    lazy val executor = Executors.newSingleThreadScheduledExecutor()
    override def whenReady(): Future[Ready] = {
      val promise = Promise[Ready]()
      val executor = Executors.newSingleThreadScheduledExecutor()
      executor.schedule(new Runnable {
        override def run(): Unit = {
          promise.success(Ready)
          executor.shutdown()
        }
      }, duration.toMillis, TimeUnit.MILLISECONDS)
      promise.future
    }
    override protected def timeOut(): Unit = executor.shutdown()
  }

  def healthCheck[R](url: String,
                     interval: FiniteDuration = 1.second,
                     httpMethod: String = "HEAD",
                     awaitTimeout: FiniteDuration = 1.minute,
                     connectionTimeout: FiniteDuration = 1.second,
                     readTimeout: FiniteDuration = 1.second) = new ReadinessNotifier with StrictLogging {
    override val defaultTimeout = awaitTimeout
    val connectionTimeoutMs = connectionTimeout.toMillis.toInt
    val readTimeoutMs = readTimeout.toMillis.toInt
    lazy val executor = Executors.newSingleThreadScheduledExecutor()

    override protected def timeOut(): Unit = executor.shutdown()

    override protected def timeOutMessage(duration: Duration) =
      s"Resource ($url) is not ready within duration ($duration)"

    override def whenReady(): Future[Ready] = {
      val promise = Promise[Ready]()
      executor.scheduleAtFixedRate(new Runnable {
        override def run(): Unit = {
          val response = Get(url).timeout(connectionTimeoutMs, readTimeoutMs).method(httpMethod).asString
          logger.debug(s"Health check response: ${response.code}")
          if (response.isNotError) {
            promise.success(Ready)
            executor.shutdown()
          }
        }
      }, 0, interval.toMillis, TimeUnit.MILLISECONDS)
      promise.future
    }
  }


}
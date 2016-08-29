package org.zalando.test.kit.service

import java.sql.{Connection, DriverManager}

import org.scalatest.{FlatSpec, MustMatchers}

class DatabaseTestServiceSpec extends FlatSpec with MustMatchers {

  val testService = new DatabaseTestService("Database Test Server", port = 15432)

  behavior of "DatabaseTestService"

  it must "start and stop" in {
    var conn: Connection = null
    try {
      testService.start()
      conn = DriverManager.getConnection("jdbc:postgresql://localhost:15432/postgres?user=postgres")
    } finally {
      if (conn != null)
        conn.close()
      testService.stop()
    }
  }

}

package com.github.unisay.test.kit.service

import java.sql.{Connection, DriverManager}

import org.scalatest.{FlatSpec, MustMatchers}
import com.github.unisay.test.kit.ScalatestServiceKit
import com.github.unisay.test.kit.service.TestService.Composition

class DatabaseTestServiceSpec extends FlatSpec with MustMatchers with ScalatestServiceKit {

  val database = new DatabaseTestService("Database Test Server", port = 15432) with SuiteLifecycle

  def testServices: Composition = database

  behavior of "DatabaseTestService"

  it must "start and stop" in {
    var conn: Connection = null
    try {
      conn = DriverManager.getConnection("jdbc:postgresql://localhost:15432/postgres?user=postgres")
    } finally {
      if (conn != null)
        conn.close()
    }
  }
}

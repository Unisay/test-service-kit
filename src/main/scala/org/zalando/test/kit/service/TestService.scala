package org.zalando.test.kit.service

trait TestService {

  def name: String

  def beforeSuite(): Unit = {}

  def beforeTest(): Unit = {}

  def afterTest(): Unit = {}

  def afterSuite(): Unit = {}

}

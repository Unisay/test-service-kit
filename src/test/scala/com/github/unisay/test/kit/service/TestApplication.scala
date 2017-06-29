package com.github.unisay.test.kit.service

object TestApplication {

  def main(args: Array[String]): Unit = {
    println("Test Application started with arguments: " + args.mkString(" "))
    while (true) {
      println("Working...")
      Thread.sleep(1000)
    }
  }

}

import sbt.Keys._

name := "test-service-kit"

organization := "com.github.Unisay"

version := "7.0.0"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.11.8", scalaVersion.value)

val specs2Ver = "3.8.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.6.0"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += ("org.scalatest" %% "scalatest" % "3.0.1")
libraryDependencies += "org.specs2" %% "specs2-core" % specs2Ver
libraryDependencies += "org.specs2" %% "specs2-junit" % specs2Ver
libraryDependencies += "org.specs2" %% "specs2-gwt" % specs2Ver

libraryDependencies += "com.github.kxbmap" %% "configs" % "0.4.4" % "test"
libraryDependencies += "com.typesafe" % "config" % "1.3.1" % "test"

lazy val providedDependencies = Seq(
  ("org.mock-server" % "mockserver-netty" % "3.10.4")
    .exclude("org.scala-lang.modules", "scala-parser-combinators_2.11"),
  "com.github.docker-java" % "docker-java" % "2.1.4",
  "com.opentable.components" % "otj-pg-embedded" % "0.7.1")

libraryDependencies ++= providedDependencies.map(_ % "provided")

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

fork in Test := true

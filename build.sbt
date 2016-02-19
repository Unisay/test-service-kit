import sbt.Keys._

name := "test-service-kit"

organization := "org.zalando"

version := "4.0.0-SNAPSHOT"

isSnapshot := true

scalaVersion := "2.11.7"

publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

sonatypeProfileName := "org.zalando"

pomExtra := (
  <url>https://github.com/zalando/scala-jsonapi</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:zalando/test-service-kit.git</url>
    <connection>scm:git:git@github.com:zalando/test-service-kit.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ylazaryev</id>
      <name>Yuriy Lazaryev</name>
      <url>https://github.com/Unisay</url>
    </developer>
  </developers>
)

libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.14"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6"
libraryDependencies += "com.github.kxbmap" % "configs_2.11" % "0.3.0" % "test"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.2.1" % "test"
libraryDependencies += "com.typesafe" % "config" % "1.3.0" % "test"

lazy val providedDependencies = Seq(
  "org.mock-server" % "mockserver-netty" % "3.10.2",
  "com.github.docker-java" % "docker-java" % "2.1.4",
  "com.opentable.components" % "otj-pg-embedded" % "0.4.4"
)

libraryDependencies ++= providedDependencies.map(_ % "provided")

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
import sbt.Keys._

name := "test-service-kit"

organization := "org.zalando"

version := "5.0.2"

isSnapshot := false

scalaVersion := "2.11.8"

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

//usePgpKeyHex("BF5E171FB106E7AD")

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

val specs2Ver = "3.8.4"
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.21"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.2.1"
libraryDependencies += "org.specs2" %% "specs2-core" % specs2Ver
libraryDependencies += "org.specs2" %% "specs2-junit" % specs2Ver
libraryDependencies += "org.specs2" %% "specs2-gwt" % specs2Ver

libraryDependencies += "com.github.kxbmap" % "configs_2.11" % "0.3.0" % "test"
libraryDependencies += "com.typesafe" % "config" % "1.3.0" % "test"

lazy val providedDependencies = Seq(
  "org.mock-server" % "mockserver-netty" % "3.10.4",
  "com.github.docker-java" % "docker-java" % "2.1.4",
  "com.opentable.components" % "otj-pg-embedded" % "0.7.1"
)

libraryDependencies ++= providedDependencies.map(_ % "provided")

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

fork in Test := true

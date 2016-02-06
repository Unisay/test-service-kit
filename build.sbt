import _root_.sbt.Resolver
import sbt.Keys._

name := "test-service-kit"

organization := "org.zalando"

version := "0.3.0-SNAPSHOT"

isSnapshot := true

scalaVersion := "2.11.7"

publishTo := {
    val nexus = "https://maven.zalando.net/"
    val realm = "Sonatype Nexus Repository Manager API"
    if (isSnapshot.value)
        Some(realm at nexus + "content/repositories/snapshots")
    else
        Some(realm at nexus + "content/repositories/releases")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := true

resolvers += Resolver.mavenLocal
resolvers += "Sonatype Nexus Repository Manager" at "https://maven.zalando.net/content/groups/public"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.14"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6"

lazy val providedDependencies = Seq(
  "org.mock-server" % "mockserver-netty" % "3.10.2",
  "com.github.docker-java" % "docker-java" % "2.1.2",
  "com.opentable.components" % "otj-pg-embedded" % "0.4.4"
)

libraryDependencies ++= providedDependencies.map(_ % "provided")

import com.typesafe.config.ConfigFactory
import sbt._
import sbt.Keys._
import play.Project._
import net.litola.SassPlugin

object ApplicationBuild extends Build {
  val appName         = "c3"

  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "org.specs2" %% "specs2" % "1.14" % "test" withSources() withJavadoc(),
    "org.mockito" % "mockito-all" % "1.9.5" % "test" withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-testkit" % "2.2.1" % "test" withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-agent" % "2.2.1" % "test" withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-actor" % "2.2.1" % "test" withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-remote" % "2.2.1" % "test" withSources() withJavadoc(),
    "com.dwp.carers" %% "carerscommon" % "0.3" ,
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "me.moocar" % "logback-gelf" % "0.9.6p2",
    "com.google.inject" % "guice" % "3.0",
    "com.tzavellas" % "sse-guice" % "0.7.1"
  )

  var sO: Seq[Project.Setting[_]] = Seq(scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-Xlint", "-language:reflectiveCalls"))

  var sV: Seq[Project.Setting[_]] = Seq(scalaVersion := "2.10.2")

  var sR: Seq[Project.Setting[_]] = Seq(resolvers += "Carers repo" at "http://build.3cbeta.co.uk:8080/artifactory/repo/")

  var sTest: Seq[Project.Setting[_]] = Seq()

  if (System.getProperty("include") != null ) {

    sTest = Seq(testOptions in Test += Tests.Argument("include", System.getProperty("include")))
  }

  if (System.getProperty("exclude") != null ) {
    sTest = Seq(testOptions in Test += Tests.Argument("exclude", System.getProperty("exclude")))
  }

  var jO: Seq[Project.Setting[_]] = Seq(javaOptions in Test += System.getProperty("waitSeconds"))

  var gS: Seq[Project.Setting[_]] = Seq(concurrentRestrictions in Global := Seq(Tags.limit(Tags.CPU, 4), Tags.limit(Tags.Network, 10), Tags.limit(Tags.Test, 4)))

  var f: Seq[Project.Setting[_]] = Seq(sbt.Keys.fork in Test := false)

  var appSettings: Seq[Project.Setting[_]] =  SassPlugin.sassSettings ++ sV ++ sO ++ sR ++ gS ++ sTest ++ f ++ jO

  val main = play.Project(appName, appVersion, appDependencies).settings(appSettings: _*)
}
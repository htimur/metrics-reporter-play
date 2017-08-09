import sbt._
import sbt.Keys._
import bintray.BintrayKeys._

object Common {
  lazy val settings = Seq(
    bintrayReleaseOnPublish := isSnapshot.value,
    bintrayPackage := "metrics-reporter-play",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
  )

  val metricsVersion = "3.2.4"
  val playVersion = "2.5.16"

  lazy val libraryDependencies = Seq(
    "com.typesafe.play" %% "play" % playVersion % Provided,
    "io.dropwizard.metrics" % "metrics-core" % metricsVersion % Provided,
    "com.wix" %% "accord-core" % "0.7.1",
    //test
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % Test
  )

}

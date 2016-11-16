import sbt._
import sbt.Keys._
import bintray.BintrayKeys._

object Common {
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishArtifact := true,
    publishArtifact in Test := false,
    bintrayReleaseOnPublish := isSnapshot.value,
    bintrayPackage := "metrics-reporter-play",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
  )

  val metricsVersion = "3.1.2"
  val playVersion = "2.5.3"

  lazy val libraryDependencies = Seq(
    "com.typesafe.play" %% "play" % playVersion % Provided,
    "de.threedimensions" %% "metrics-play" % "2.5.13",
    "com.wix" %% "accord-core" % "0.6",
    //test
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
  )

}
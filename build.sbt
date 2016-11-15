name := "metrics-reporter-play"
organization in ThisBuild := "de.khamrakulov"

scalaVersion in ThisBuild := "2.11.8"

crossScalaVersions in ThisBuild := Seq("2.11.8")

scalacOptions in ThisBuild := Seq("-deprecation", "-unchecked", "-feature")

val metricsVersion = "3.1.2"
val playVersion = "2.5.3"

libraryDependencies in ThisBuild ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % Provided,
  "de.threedimensions" %% "metrics-play" % "2.5.13",
  "com.wix" %% "accord-core" % "0.6",
  //test
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

lazy val root = Project(
  id = "root",
  base = file("."),
  aggregate = Seq(core, graphite)
).dependsOn(core, graphite)

lazy val core = project in file("core")

lazy val graphite = (project in file("graphite")).settings(
  libraryDependencies ++= Seq(
    "io.dropwizard.metrics" % "metrics-graphite" % metricsVersion
  )
).dependsOn(core)


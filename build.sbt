name := "metrics-reporter-play"
organization in ThisBuild := "de.khamrakulov.metrics-reporter-play"
scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild := Seq("-deprecation", "-unchecked", "-feature")

resolvers in ThisBuild += Resolver.typesafeRepo("release")
resolvers in ThisBuild += Resolver.jcenterRepo

lazy val root = (project in file("."))
  .settings(
    publish := {}
  )
  .aggregate(core, graphite, ganglia)

lazy val core = (project in file("core"))
  .settings(Common.settings)
  .settings(
    name := "reporter-core",
    libraryDependencies ++= Common.libraryDependencies
  )

lazy val graphite = (project in file("graphite"))
  .settings(Common.settings)
  .settings(
    name := "reporter-graphite",
    libraryDependencies ++= Common.libraryDependencies ++ Seq(
      "io.dropwizard.metrics" % "metrics-graphite" % Common.metricsVersion
    )
  ).dependsOn(core)

lazy val ganglia = (project in file("ganglia"))
  .settings(Common.settings)
  .settings(
    name := "reporter-ganglia",
    libraryDependencies ++= Common.libraryDependencies ++ Seq(
      "io.dropwizard.metrics" % "metrics-ganglia" % Common.metricsVersion
    )
  ).dependsOn(core)


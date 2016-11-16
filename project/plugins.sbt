
resolvers in ThisBuild += Resolver.typesafeRepo("release")
resolvers in ThisBuild += Resolver.jcenterRepo
//resolvers in ThisBuild += Resolver.bintrayRepo("breadfan", "maven")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.2.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")
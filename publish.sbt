publishMavenStyle in ThisBuild := true

publishArtifact in ThisBuild := true

publishArtifact in Test := false

publishMavenStyle in ThisBuild := false

bintrayRepository in bintray := "maven"

bintrayPackage in bintray := "metrics-reporter-play"

bintrayReleaseOnPublish in bintray := isSnapshot.value

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
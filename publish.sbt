publishMavenStyle in ThisBuild := false

bintrayRepository in ThisBuild := "maven"

bintrayPackage in ThisBuild := "metrics-reporter-play"

bintrayOrganization in ThisBuild := Some("htimur")

bintrayReleaseOnPublish in ThisBuild := isSnapshot.value

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
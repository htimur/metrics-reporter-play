publishMavenStyle in bintray in ThisBuild := false

bintrayRepository in bintray in ThisBuild := "maven"

bintrayOrganization in bintray in ThisBuild := Some("htimur")

bintrayReleaseOnPublish in bintray in ThisBuild := isSnapshot.value

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
package de.khamrakulov.play.metrics.graphite

import scala.collection.JavaConversions._

import org.scalatest.{FlatSpec, Matchers}
import play.api.{Configuration, Environment}

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class ConfigCheckSpec extends FlatSpec with Matchers {
  val env = Environment.simple()
  val config = Configuration.load(env)

  "Configuration" should "have new factory type" in {
    val configurations = asScalaBuffer(config.getConfigList("metrics.factories").get)
    configurations.size shouldBe 2
  }
}

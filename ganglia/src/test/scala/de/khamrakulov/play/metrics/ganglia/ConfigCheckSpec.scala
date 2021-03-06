package de.khamrakulov.play.metrics.ganglia

import org.scalatest.{FlatSpec, Matchers}
import play.api.{Configuration, Environment}

import scala.collection.JavaConversions._

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

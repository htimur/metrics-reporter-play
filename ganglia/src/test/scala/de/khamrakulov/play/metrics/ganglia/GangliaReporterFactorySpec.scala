package de.khamrakulov.play.metrics.ganglia

import java.util.UUID

import com.codahale.metrics.MetricRegistry
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode
import org.scalatest.{FlatSpec, Matchers}
import play.api.{Configuration, Environment}

import scala.concurrent.duration._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class GangliaReporterFactorySpec extends FlatSpec with Matchers {
  val env = Environment.simple()
  val config = Configuration.load(env)

  "GangliaReporterFactory" should "properly parse config with defaults" in {
    val c = GangliaReporterFactory.config(config.getConfig("metrics.reporterPlain").get)
    c.durationUnit shouldBe MILLISECONDS
    c.frequency shouldBe 1.minute
    c.rateUnit shouldBe SECONDS
    c.host shouldBe "localhost"
    c.port shouldBe 8649
    c.prefix shouldBe ""
    c.ttl shouldBe 1
    c.mode shouldBe UDPAddressingMode.UNICAST
    c.uuid shouldBe None
    c.spoof shouldBe None
    c.tmax shouldBe 60
    c.dmax shouldBe 0
  }

  it should "properly parse config with all values" in {
    val c = GangliaReporterFactory.config(config.getConfig("metrics.reporterBasic").get)
    c.durationUnit shouldBe SECONDS
    c.frequency shouldBe 10.seconds
    c.rateUnit shouldBe MINUTES
    c.host shouldBe "localhost2"
    c.port shouldBe 9090
    c.prefix shouldBe "some"
    c.ttl shouldBe 2
    c.mode shouldBe UDPAddressingMode.MULTICAST
    c.uuid shouldBe Some(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))
    c.spoof shouldBe Some("test")
    c.tmax shouldBe 1
    c.dmax shouldBe 1

  }

  it should "not create factory for incorrect config" in {
    GangliaReporterFactory(
      new MetricRegistry(), config.getConfig("metrics.gangliaIncorrectConfig").get
    ) shouldBe None
  }
}

package de.khamrakulov.play.metrics.console

import java.time.ZoneId
import java.time.zone.ZoneRulesException

import com.codahale.metrics.MetricRegistry
import org.scalatest._
import play.api.{Configuration, Environment}

import scala.concurrent.duration._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class ConsoleReporterFactorySpec extends FlatSpec with Matchers with Assertions {
  val env = Environment.simple()
  val config = Configuration.load(env)

  "ConsoleReporterFactory" should "properly parse config with defaults" in {
    val c = ConsoleReporterFactory.config(config.getConfig("metrics.reporterPlain").get)
    c.timeZone shouldBe ZoneId.of("UTC")
    c.durationUnit shouldBe MILLISECONDS
    c.frequency shouldBe 1.minute
    c.output shouldBe "stdout"
    c.rateUnit shouldBe SECONDS
  }

  it should "properly parse config with all values" in {
    val c = ConsoleReporterFactory.config(config.getConfig("metrics.reporterBasic").get)
    c.timeZone shouldBe ZoneId.of("CET")
    c.durationUnit shouldBe SECONDS
    c.frequency shouldBe 10.seconds
    c.output shouldBe "stderr"
    c.rateUnit shouldBe MINUTES
  }

  it should "throw an exception if unknown timezone is specified" in {
    intercept[ZoneRulesException] {
      ConsoleReporterFactory.config(config.getConfig("metrics.reporterWithIncorrectValues1").get)
    }
  }

  it should "throw an exception if incorrect frequency is specified" in {
    intercept[NumberFormatException] {
      ConsoleReporterFactory.config(config.getConfig("metrics.reporterWithIncorrectValues2").get)
    }
  }

  it should "not create factory for incorrect config" in {
    ConsoleReporterFactory(
      new MetricRegistry(), config.getConfig("metrics.reporterBasicIncorrect").get
    ) shouldBe None
  }
}

package de.khamrakulov.play.metrics.graphite

import com.codahale.metrics.MetricRegistry
import org.scalatest.{FlatSpec, Matchers}
import play.api.{Configuration, Environment}

import scala.concurrent.duration._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class GraphiteReporterFactorySpec extends FlatSpec with Matchers {
  val env = Environment.simple()
  val config = Configuration.load(env)

  "GraphiteReporterFactory" should "properly parse config with defaults" in {
    val c = GraphiteReporterFactory.config(config.getConfig("metrics.reporterPlain").get)
    c.durationUnit shouldBe MILLISECONDS
    c.frequency shouldBe 1.minute
    c.rateUnit shouldBe SECONDS
    c.host shouldBe "localhost"
    c.port shouldBe 8080
    c.prefix shouldBe ""
    c.sender shouldBe a[Graphite.type]
  }

  it should "properly parse config with all values" in {
    val c = GraphiteReporterFactory.config(config.getConfig("metrics.reporterBasic").get)
    c.durationUnit shouldBe SECONDS
    c.frequency shouldBe 10.seconds
    c.rateUnit shouldBe MINUTES
    c.host shouldBe "localhost2"
    c.port shouldBe 9090
    c.prefix shouldBe "some"
    c.sender shouldBe a[GraphiteUDP.type]
  }

  it should "properly parse config pickled config" in {
    val c = GraphiteReporterFactory.config(config.getConfig("metrics.reporterPickled").get)
    c.durationUnit shouldBe MILLISECONDS
    c.frequency shouldBe 1.minute
    c.rateUnit shouldBe SECONDS
    c.host shouldBe "localhost"
    c.port shouldBe 8080
    c.prefix shouldBe ""
    c.sender shouldBe a[PickledGraphite]
    c.sender.asInstanceOf[PickledGraphite].batchSize shouldBe 100
  }

  it should "properly parse config rabbitMQ config" in {
    val c = GraphiteReporterFactory.config(config.getConfig("metrics.reporterRabbitMQ").get)
    c.durationUnit shouldBe MILLISECONDS
    c.frequency shouldBe 1.minute
    c.rateUnit shouldBe SECONDS
    c.host shouldBe "localhost"
    c.port shouldBe 8080
    c.prefix shouldBe ""
    c.sender shouldBe a[GraphiteRabbitMQ]
    val sender = c.sender.asInstanceOf[GraphiteRabbitMQ]
    sender.exchange shouldBe "test3"
    sender.rabbitPassword shouldBe "test2"
    sender.rabbitUser shouldBe "test1"
  }

  it should "use graphite sender if unknown sender is specified" in {
    val c = GraphiteReporterFactory.config(config.getConfig("metrics.reporterWithUnknownSender").get)
    c.sender shouldBe a[Graphite.type]
  }

  it should "throw an exception if incorrect frequency is specified" in {
    intercept[NumberFormatException] {
      GraphiteReporterFactory.config(config.getConfig("metrics.reporterWithIncorrectFrequency").get)
    }
  }

  it should "not create factory for incorrect config" in {
    GraphiteReporterFactory(
      new MetricRegistry(), config.getConfig("metrics.graphiteIncorrectConfig").get
    ) shouldBe None
  }
}

package de.khamrakulov.play.metrics.graphite

import com.codahale.metrics.graphite.GraphiteReporter
import com.codahale.metrics.{MetricFilter, MetricRegistry, graphite}
import com.wix.accord.dsl._
import com.wix.accord.{Failure, Success, _}
import de.khamrakulov.play.metrics.{ReporterConfig, ReporterFactory}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.{Duration, _}

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
sealed trait GraphiteSender

case object Graphite extends GraphiteSender

case object GraphiteUDP extends GraphiteSender

case class PickledGraphite(batchSize: Int) extends GraphiteSender

case class GraphiteRabbitMQ(rabbitUser: String, rabbitPassword: String, exchange: String) extends GraphiteSender

case class GraphiteReporterConfig(durationUnit: TimeUnit,
                                  rateUnit: TimeUnit,
                                  frequency: Duration,
                                  host: String,
                                  port: Int,
                                  prefix: String,
                                  sender: GraphiteSender) extends ReporterConfig

object GraphiteReporterFactory extends ReporterFactory[GraphiteReporter, GraphiteReporterConfig] {
  private val logger = Logger(GraphiteReporterFactory.getClass)
  implicit val pickledGraphiteValidator = validator[PickledGraphite] { c =>
    c.batchSize should be >= 10
  }

  implicit val graphiteRabbitMQValidator = validator[GraphiteRabbitMQ] { c =>
    c.exchange is notEmpty
    c.rabbitUser is notEmpty
    c.rabbitPassword is notEmpty
  }
  implicit val graphiteReporterConfigValidator = validator[GraphiteReporterConfig] { c =>
    c.durationUnit is notNull
    c.rateUnit is notNull
    c.frequency is notNull
    c.host is notEmpty
    c.sender is notNull

    (c.port should be >= 1) and (c.port should be <= 65535)
  }

  override def config(conf: Configuration): GraphiteReporterConfig = {
    val durationUnit = timeUnits.getOrElse(conf.getString("durationUnit").getOrElse("milliseconds"), MILLISECONDS)
    val rateUnit = timeUnits.getOrElse(conf.getString("rateUnit").getOrElse("seconds"), SECONDS)
    val frequency = Duration(conf.getString("frequency").getOrElse("1 minute"))
    val host = conf.getString("host").getOrElse("localhost")
    val port = conf.getInt("port").getOrElse(8080)
    val prefix = conf.getString("prefix").getOrElse("")
    val sender = conf.getConfig("sender") match {
      case Some(senderConfig) => senderConfig.getString("type").get match {
        case "pickled" => PickledGraphite(senderConfig.getInt("batchSize").get)
        case "rabbitMQ" => GraphiteRabbitMQ(
          senderConfig.getString("rabbitUser").get,
          senderConfig.getString("rabbitPassword").get,
          senderConfig.getString("exchange").get
        )
        case "udp" => GraphiteUDP
        case _ => Graphite
      }
      case None => Graphite
    }

    GraphiteReporterConfig(durationUnit, rateUnit, frequency, host, port, prefix, sender)
  }

  override def apply(registry: MetricRegistry, conf: Configuration): Option[GraphiteReporter] = {
    val c = config(conf)

    validate(c) match {
      case Success =>
        val sender = c.sender match {
          case Graphite => new graphite.Graphite(c.host, c.port)
          case GraphiteUDP => new graphite.GraphiteUDP(c.host, c.port)
          case GraphiteRabbitMQ(user, pwd, exch) => new graphite.GraphiteRabbitMQ(
            c.host, c.port, user, pwd, exch
          )
          case PickledGraphite(batchSize) => new graphite.PickledGraphite(
            c.host, c.port, batchSize
          )
        }

        val reporter = GraphiteReporter.forRegistry(registry)
          .convertDurationsTo(c.durationUnit)
          .convertRatesTo(c.rateUnit)
          .prefixedWith(c.prefix)
          .filter(MetricFilter.ALL)
          .build(sender)
        reporter.start(c.frequency.length, c.frequency.unit)
        Some(reporter)
      case Failure(violations) =>
        violations.foreach { v => logger.error(s"${v.description} ${v.constraint}") }
        None
    }
  }
}

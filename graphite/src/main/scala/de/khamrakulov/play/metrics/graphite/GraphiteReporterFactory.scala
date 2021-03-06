package de.khamrakulov.play.metrics.graphite

import com.codahale.metrics.graphite.GraphiteReporter
import com.codahale.metrics.{MetricFilter, MetricRegistry, graphite}
import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import com.wix.accord.{Failure, Success, _}
import de.khamrakulov.play.metrics.{MetricReporter, ReporterConfig, ReporterFactory}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.{Duration, _}

/**
  * Graphite Sender
  *
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
sealed trait GraphiteSender

/**
  * Default Graphite sender
  */
case object Graphite extends GraphiteSender

/**
  * UDP Graphite sender
  */
case object GraphiteUDP extends GraphiteSender

/**
  * Pickled Graphite sender
  *
  * @param batchSize the batch size to send
  */
case class PickledGraphite(batchSize: Int) extends GraphiteSender

/**
  * RabbitMQ Graphite sender
  *
  * @param rabbitUser     RabbitMQ user
  * @param rabbitPassword RabbitMQ password
  * @param exchange       RabbitMQ exchange
  */
case class GraphiteRabbitMQ(rabbitUser: String,
                            rabbitPassword: String,
                            exchange: String) extends GraphiteSender

/** *
  * Graphite reporter config
  *
  * @param durationUnit The unit to report durations as.
  * @param rateUnit     The unit to report rates as.
  * @param frequency    The frequency to report metrics.
  * @param host         The hostname of the Graphite server to report to.
  * @param port         The port of the Graphite server to report to.
  * @param prefix       The prefix for Metric key names to report to Graphite.
  * @param sender       Sender configuration
  */
case class GraphiteReporterConfig(durationUnit: TimeUnit,
                                  rateUnit: TimeUnit,
                                  frequency: Duration,
                                  host: String,
                                  port: Int,
                                  prefix: String,
                                  sender: GraphiteSender) extends ReporterConfig

/**
  * Graphite reporter factory
  */
object GraphiteReporterFactory extends ReporterFactory[GraphiteReporterConfig] {
  private val logger = Logger(GraphiteReporterFactory.getClass)

  implicit val pickledGraphiteValidator: TransformedValidator[PickledGraphite] =
    validator[PickledGraphite] { c =>
      c.batchSize should be >= 10
    }

  implicit val graphiteRabbitMQValidator: TransformedValidator[GraphiteRabbitMQ] =
    validator[GraphiteRabbitMQ] { c =>
      c.exchange is notEmpty
      c.rabbitUser is notEmpty
      c.rabbitPassword is notEmpty
    }

  implicit val graphiteReporterConfigValidator: TransformedValidator[GraphiteReporterConfig] =
    validator[GraphiteReporterConfig] { c =>
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

  override def apply(registry: MetricRegistry, conf: Configuration): Option[MetricReporter] = {
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

        Some(MetricReporter(
          () => reporter.start(c.frequency.length, c.frequency.unit),
          () => {
            reporter.stop()
            sender.close()
          }
        ))
      case Failure(violations) =>
        violations.foreach { v => logger.error(s"${v.description} ${v.constraint}") }
        None
    }
  }
}

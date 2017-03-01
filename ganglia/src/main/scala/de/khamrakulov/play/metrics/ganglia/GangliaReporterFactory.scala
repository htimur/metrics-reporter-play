package de.khamrakulov.play.metrics.ganglia

import java.util.UUID

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.ganglia.GangliaReporter
import com.wix.accord._
import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import de.khamrakulov.play.metrics.{ReporterConfig, ReporterFactory}
import info.ganglia.gmetric4j.gmetric.GMetric
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode
import play.api.{Configuration, Logger}

import scala.concurrent.duration.{Duration, _}

/**
  * Ganglia reporter config
  *
  * @param durationUnit The unit to report durations as.
  * @param rateUnit     The unit to report rates as.
  * @param frequency    The frequency to report metrics.
  * @param host         The hostname (or group) of the Ganglia server(s) to report to.
  * @param port         The port of the Ganglia server(s) to report to.
  * @param mode         The UDP addressing mode to announce the metrics with. One of unicast or multicast.
  * @param ttl          The time-to-live of the UDP packets for the announced metrics.
  * @param prefix       The time-to-live of the UDP packets for the announced metrics.
  * @param uuid         The UUID to tag announced metrics with.
  * @param spoof        The hostname and port to use instead of this nodes for the announced metrics. In the format hostname:port.
  * @param tmax         The tmax value to announce metrics with.
  * @param dmax         The dmax value to announce metrics with.
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
case class GangliaReporterConfig(durationUnit: TimeUnit,
                                 rateUnit: TimeUnit,
                                 frequency: Duration,
                                 host: String,
                                 port: Int,
                                 mode: UDPAddressingMode,
                                 ttl: Int,
                                 prefix: String,
                                 uuid: Option[UUID],
                                 spoof: Option[String],
                                 tmax: Int,
                                 dmax: Int
                                ) extends ReporterConfig

/**
  * Ganglia reporter factory
  */
object GangliaReporterFactory extends ReporterFactory[GangliaReporter, GangliaReporterConfig] {

  private val logger = Logger(GangliaReporterFactory.getClass)

  implicit val gangliaReporterConfigValidator: TransformedValidator[GangliaReporterConfig] =
    validator[GangliaReporterConfig] { c =>
      c.durationUnit is notNull
      c.rateUnit is notNull
      c.frequency is notNull
      c.host is notEmpty
      c.prefix is notNull
      (c.port should be >= 1) and (c.port should be <= 65535)
      c.mode is in(UDPAddressingMode.UNICAST, UDPAddressingMode.MULTICAST)
      c.ttl should be >= 0
      c.tmax should be >= 0
      c.dmax should be >= 0
    }

  override def config(conf: Configuration): GangliaReporterConfig = {
    val durationUnit = timeUnits.getOrElse(conf.getString("durationUnit").getOrElse("milliseconds"), MILLISECONDS)
    val rateUnit = timeUnits.getOrElse(conf.getString("rateUnit").getOrElse("seconds"), SECONDS)
    val frequency = Duration(conf.getString("frequency").getOrElse("1 minute"))
    val host = conf.getString("host").getOrElse("localhost")
    val port = conf.getInt("port").getOrElse(8649)
    val mode = conf.getString("mode") match {
      case Some(modeName) => UDPAddressingMode.valueOf(modeName.toUpperCase)
      case None => UDPAddressingMode.UNICAST
    }
    val prefix = conf.getString("prefix").getOrElse("")
    val ttl = conf.getInt("ttl").getOrElse(1)
    val uuid = conf.getString("uuid").map(UUID.fromString)
    val spoof = conf.getString("spoof")
    val tmax = conf.getInt("tmax").getOrElse(60)
    val dmax = conf.getInt("dmax").getOrElse(0)

    GangliaReporterConfig(durationUnit, rateUnit, frequency, host, port, mode, ttl, prefix, uuid, spoof, tmax, dmax)
  }

  override def apply(registry: MetricRegistry, conf: Configuration): Option[GangliaReporter] = {
    val c = config(conf)

    validate(c) match {
      case Success =>
        val ganglia = new GMetric(c.host,
          c.port,
          c.mode,
          c.ttl,
          c.uuid.isDefined || c.spoof.isDefined,
          c.uuid.orNull,
          c.spoof.orNull
        )
        val reporter = GangliaReporter
          .forRegistry(registry)
          .convertDurationsTo(c.durationUnit)
          .convertRatesTo(c.rateUnit)
          .prefixedWith(c.prefix)
          .withDMax(c.dmax)
          .withTMax(c.tmax)
          .build(ganglia)

        Some(reporter)
      case Failure(violations) =>
        violations.foreach { v => logger.error(s"${v.description} ${v.constraint}") }
        None
    }
  }
}

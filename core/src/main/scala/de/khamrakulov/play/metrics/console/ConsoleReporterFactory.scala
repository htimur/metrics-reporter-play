package de.khamrakulov.play.metrics.console

import java.time.ZoneId
import java.util.TimeZone

import com.codahale.metrics.{ConsoleReporter, MetricRegistry}
import com.wix.accord._
import com.wix.accord.dsl._
import de.khamrakulov.play.metrics.{ReporterConfig, ReporterFactory}
import play.api.{Configuration, Logger}

import scala.concurrent.duration._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  *
  * @param durationUnit The unit to report durations as.
  * @param rateUnit The unit to report rates as.
  * @param frequency The frequency to report metrics.
  * @param timeZone The timezone to display dates/times for.
  * @param output The stream to write to. One of stdout or stderr.
  */
case class ConsoleReporterConfig(durationUnit: TimeUnit,
                                 rateUnit: TimeUnit,
                                 frequency: Duration,
                                 timeZone: ZoneId,
                                 output: String) extends ReporterConfig

/**
  * Console reporter factory
  */
object ConsoleReporterFactory extends ReporterFactory[ConsoleReporter, ConsoleReporterConfig] {
  private val logger = Logger(classOf[ConsoleReporterConfig])

  implicit val consoleReporterConfigValidator = validator[ConsoleReporterConfig] { c =>
    c.durationUnit is notNull
    c.rateUnit is notNull
    c.frequency is notNull
    c.timeZone is notNull
    c.output is in("stdout", "stderr")
  }

  override def apply(registry: MetricRegistry, conf: Configuration): Option[ConsoleReporter] = {
    val c = config(conf)

    validate(c) match {
      case Success =>
        val reporter = ConsoleReporter
          .forRegistry(registry)
          .convertDurationsTo(c.durationUnit)
          .convertRatesTo(c.rateUnit)
          .formattedFor(TimeZone.getTimeZone(c.timeZone))
          .build()
        reporter.start(c.frequency.length, c.frequency.unit)
        Some(reporter)
      case Failure(violations) =>
        violations.foreach { v => logger.error(s"${v.description} ${v.constraint}") }
        None
    }
  }

  override def config(conf: Configuration) = {
    val durationUnit = timeUnits.getOrElse(conf.getString("durationUnit").getOrElse("milliseconds"), MILLISECONDS)
    val rateUnit = timeUnits.getOrElse(conf.getString("rateUnit").getOrElse("seconds"), SECONDS)
    val frequency = Duration(conf.getString("frequency").getOrElse("1 minute"))
    val timeZone = ZoneId.of(conf.getString("timeZone").getOrElse("UTC"))
    val output = conf.getString("output").getOrElse("stdout")

    ConsoleReporterConfig(durationUnit, rateUnit, frequency, timeZone, output)
  }
}


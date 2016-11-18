package de.khamrakulov.play.metrics

import com.codahale.metrics.{MetricRegistry, ScheduledReporter}
import play.api.Configuration

import scala.concurrent.duration._

/**
  * Metrics Reporter factory interface
  *
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
trait ReporterFactory[T <: ScheduledReporter, K <: ReporterConfig] {
  val timeUnits = Map(
    "days" -> DAYS,
    "hours" -> HOURS,
    "microseconds" -> MICROSECONDS,
    "milliseconds" -> MILLISECONDS,
    "minutes" -> MINUTES,
    "nanoseconds" -> NANOSECONDS,
    "seconds" -> SECONDS
  )

  /**
    * Build reporter configuration from reporter play Configuration instance
    *
    * @param conf reporter config instance
    * @return Reporter configuration
    */
  def config(conf: Configuration): K

  /**
    * Factory method
    *
    * @param registry Metric Registry to register reporter to
    * @param conf reporter configuration instance
    * @return optional instance of reporter
    */
  def apply(registry: MetricRegistry, conf: Configuration): Option[T]
}

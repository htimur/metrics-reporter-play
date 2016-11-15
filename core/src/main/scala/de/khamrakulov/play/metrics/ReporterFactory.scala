package de.khamrakulov.play.metrics

import com.codahale.metrics.{MetricRegistry, ScheduledReporter}
import play.api.Configuration

import scala.concurrent.duration._

/**
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

  def config(conf: Configuration): K

  def apply(registry: MetricRegistry, conf: Configuration): Option[T]
}

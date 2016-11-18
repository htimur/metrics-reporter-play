package de.khamrakulov.play.metrics

import scala.concurrent.duration._

/**
  * Metrics Reporter configuration interface
  *
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
trait ReporterConfig {

  /**
    * The unit to report durations as.
    *
    * @return time unit
    */
  def durationUnit: TimeUnit

  /**
    * The unit to report rates as.
    *
    * @return time unit
    */
  def rateUnit: TimeUnit

  /**
    * The frequency to report metrics.
    *
    * @return frequency duration
    */
  def frequency: Duration
}

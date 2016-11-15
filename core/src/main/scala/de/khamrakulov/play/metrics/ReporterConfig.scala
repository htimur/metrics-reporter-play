package de.khamrakulov.play.metrics

import scala.concurrent.duration._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
trait ReporterConfig {

  def durationUnit: TimeUnit

  def rateUnit: TimeUnit

  def frequency: Duration
}

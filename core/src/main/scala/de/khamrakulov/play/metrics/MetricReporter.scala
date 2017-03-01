package de.khamrakulov.play.metrics

import scala.concurrent.{ExecutionContext, Future}

/**
  * Reporter wrapper to enable custom start and stop handlers
  *
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
object MetricReporter {
  def apply(startHook: () => Unit, stopHook: () => Unit): MetricReporter =
    new MetricReporter(startHook, stopHook)
}

class MetricReporter(startHook: () => Unit, stopHook: () => Unit) {

  /**
    * Execute start hook
    *
    * @return start handler result
    */
  def start()(implicit executor: ExecutionContext) = Future {
    startHook()
  }

  /**
    * Execute stop hook
    *
    * @return stop handler result
    */
  def stop()(implicit executor: ExecutionContext) = Future {
    stopHook()
  }
}

package de.khamrakulov.play.metrics

import javax.inject.{Inject, Named, Singleton}

import play.api.Logger
import play.api.inject.ApplicationLifecycle

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  *
  * LifecycleManager starts and stops reporters on application startup and stop
  */
@Singleton
class ReporterLifecycleManager @Inject()(@Named("metrics.reporters") reporterRegistry: MetricsReporterRegistry, lifecycle: ApplicationLifecycle) {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val logger = Logger(classOf[ReporterLifecycleManager])

  reporterRegistry.reporters.foreach { reporter =>
    val reporterName = reporter.getClass.getSimpleName
    logger.info(s"Starting $reporterName")
    reporter.start()

    lifecycle.addStopHook { () =>
      logger.info(s"Stopping $reporterName")
      reporter.stop()
    }
  }
}

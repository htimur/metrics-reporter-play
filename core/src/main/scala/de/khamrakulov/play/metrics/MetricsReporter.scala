package de.khamrakulov.play.metrics

import com.codahale.metrics.{MetricRegistry, ScheduledReporter, SharedMetricRegistries}
import com.kenshoo.play.metrics.{DisabledMetrics, Metrics, MetricsImpl}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment, Logger}

import scala.collection.JavaConversions._

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class MetricsReporter extends Module {
  private val logger = Logger(classOf[MetricsReporter])

  def getFactories(configs: java.util.List[Configuration]): Map[String, Class[_ <: ReporterFactory[ScheduledReporter, _]]] = configs.map { c =>
    val reporterType = c.getString("type").get
    val factory = Class.forName(c.getString("path").get + "$").asSubclass(classOf[ReporterFactory[ScheduledReporter, _]])
    reporterType -> factory
  }.toMap[String, Class[_ <: ReporterFactory[ScheduledReporter, _]]]

  def createReporters(configuration: Configuration, registry: MetricRegistry): List[ScheduledReporter] =
    configuration.getConfig("metrics") match {
      case Some(conf) =>
        val factories = getFactories(conf.getConfigList("factories").get)
        conf.getConfigList("reporters") match {
          case Some(reporters) => reporters.flatMap { c =>
            val reporterType = c.getString("type").get
            val obj = factories(reporterType)
              .getField("MODULE$")
              .get(classOf[ReporterFactory[ScheduledReporter, _]])
              .asInstanceOf[ReporterFactory[ScheduledReporter, _]]
            obj.apply(registry, c)
          }.toList
          case None =>
            logger.error("Reporters are not defined")
            List.empty[ScheduledReporter]
        }
      case _ =>
        logger.error("Metrics config is not defined")
        List.empty[ScheduledReporter]
    }

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val registry = SharedMetricRegistries.getOrCreate(
      configuration.getString("metrics.registry").getOrElse("default")
    )

    createReporters(configuration, registry)

    if (configuration.getBoolean("metrics.enabled").getOrElse(true)) {
      Seq(
        bind[MetricRegistry].toInstance(registry),
        bind[Metrics].to[MetricsImpl].eagerly
      )
    } else {
      Seq(
        bind[MetricRegistry].toInstance(registry),
        bind[Metrics].to[DisabledMetrics].eagerly
      )
    }
  }
}

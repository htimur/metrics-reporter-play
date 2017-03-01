package de.khamrakulov.play.metrics

import com.codahale.metrics.{MetricRegistry, SharedMetricRegistries}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment, Logger}

import scala.collection.JavaConversions._

/**
  * Metric reporter module
  *
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class MetricsReporterModule extends Module {
  private val logger = Logger(classOf[MetricsReporterModule])

  private type ReporterFactoryClass = Class[_ <: ReporterFactory[_]]

  /**
    * Collect type -> class map from factories in configuration
    *
    * @param configs factory configurations
    * @return the mapping of type to factory class
    */
  private[metrics] def getFactories(configs: java.util.List[Configuration]): Map[String, ReporterFactoryClass] =
    configs.map { c =>
      val reporterType = c.getString("type").get
      val factory = Class.forName(c.getString("path").get).asSubclass(classOf[ReporterFactory[_]])
      reporterType -> factory
    }.toMap[String, Class[_ <: ReporterFactory[_]]]

  /**
    * Crete reporter instances that are specified in configuration
    *
    * @param configuration application configuration
    * @param registry      metric registry to use
    * @return the list of reporters
    */
  private[metrics] def createReporters(configuration: Configuration, registry: MetricRegistry): List[MetricReporter] =
    configuration.getConfig("metrics") match {
      case Some(conf) =>
        val factories = getFactories(conf.getConfigList("factories").get)
        conf.getConfigList("reporters") match {
          case Some(reporters) => reporters.flatMap { c =>
            val reporterType = c.getString("type").get
            val obj = factories(reporterType)
              .getField("MODULE$")
              .get(classOf[ReporterFactory[_]])
              .asInstanceOf[ReporterFactory[_]]
            obj.apply(registry, c)
          }.toList
          case None =>
            logger.error("Reporters are not defined")
            List.empty[MetricReporter]
        }
      case _ =>
        logger.error("Metrics config is not defined")
        List.empty[MetricReporter]
    }

  /**
    * Get the bindings provided by this module.
    *
    * Implementations are strongly encouraged to do *nothing* in this method other than provide bindings.  Startup
    * should be handled in the constructors and/or providers bound in the returned bindings.  Dependencies on other
    * modules or components should be expressed through constructor arguments.
    *
    * The configuration and environment a provided for the purpose of producing dynamic bindings, for example, if what
    * gets bound depends on some configuration, this may be read to control that.
    *
    * @param environment   The environment
    * @param configuration The configuration
    * @return A sequence of bindings
    */
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val registry = SharedMetricRegistries.getOrCreate(
      configuration.getString("metrics.registry").get
    )

    val reporters = createReporters(configuration, registry)

    Seq(
      bind[MetricsReporterRegistry]
        .qualifiedWith("metrics.reporters")
        .toInstance(MetricsReporterRegistry(reporters)),
      bind[ReporterLifecycleManager].toSelf.eagerly()
    )
  }
}

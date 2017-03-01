package de.khamrakulov.play.metrics

import com.codahale.metrics.MetricRegistry
import de.khamrakulov.play.metrics.console.ConsoleReporterFactory
import org.scalatest._
import play.api.inject.BindingKey
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Configuration, Environment}

/**
  * @author Timur Khamrakulov <timur.khamrakulov@gmail.com>.
  */
class MetricsReporterModuleSpec extends FlatSpec with Matchers {
  val env = Environment.simple()
  val config = Configuration.load(env)

  "MetricsReporter" should "properly create factories from configs" in {
    val module = new MetricsReporterModule()
    val factories = module.getFactories(config.getConfigList("metrics.factories").get)

    factories should not be null
    factories.size should equal(1)
    factories shouldBe Map("console" -> ConsoleReporterFactory.getClass)
  }

  it should "create reporters from specified factories" in {
    val module = new MetricsReporterModule()
    val reporters = module.createReporters(config, new MetricRegistry())

    reporters should not be null
    reporters should not be empty
    reporters.head shouldBe a[MetricReporter]
  }

  it should "return empty list if reporters are not defined" in {
    val module = new MetricsReporterModule()
    val reporters1 = module.createReporters(Configuration(), new MetricRegistry())

    reporters1 shouldBe empty

    val reporters2 = module.createReporters(config.getConfig("metrics.emptyMetrics").get, new MetricRegistry())

    reporters2 shouldBe empty
  }

  it should "register all bindings correctly" in {
    val application = new GuiceApplicationBuilder()
      .bindings(new MetricsReporterModule)
      .build()
    val manager = application.injector.instanceOf[ReporterLifecycleManager]
    val reporterProvider = application.injector
      .instanceOf(
        BindingKey(classOf[MetricsReporterRegistry]).qualifiedWith("metrics.reporters")
      )

    manager shouldBe a[ReporterLifecycleManager]
    reporterProvider.reporters.size shouldBe 1
  }
}

[![Build Status](https://travis-ci.org/htimur/metrics-reporter-play.svg?branch=master)](https://travis-ci.org/htimur/metrics-reporter-play)
[![Download](https://api.bintray.com/packages/htimur/maven/metrics-reporter-play/images/download.svg) ](https://bintray.com/htimur/maven/metrics-reporter-play/_latestVersion)

## Dependencies

* [Play Framework](https://github.com/playframework/playframework)

# Quick Start

### Get the artifacts

Artifacts are released in [Bintray](https://bintray.com/). For sbt, use `resolvers += Resolver.jcenterRepo`, for gradle, use the `jcenter()` repository. For maven, [go here](https://bintray.com/htimur/maven/metrics-annotaion-play) and click "Set me up".

SBT:

```scala
libraryDependencies += Seq(
  "com.typesafe.play" %% "play" % "2.5.3",
  "de.khamrakulov.metrics-reporter-play" %% "reporter-core" % "1.0.0"
)
```

Maven:
```xml
<dependency>
  <groupId>de.khamrakulov.metrics-reporter-play</groupId>
  <artifactId>reporter-core_2.11</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

Gradle:
```groovy
compile 'de.khamrakulov.metrics-reporter-play:reporter-core_2.11:1.0.0'
```

The module `MetricsReporter` will be automatically enabled.

### Use it

The `MetricsReporter` will create and appropriately register reporters for configured registry.

### Configuration Reference

##### Metrics

```hocon
metrics {
  registry = "default"
}
```

| Name     | Default | Description              |
| :---     | :---    | :---                     |
| registry | default | Registry name to be used |


##### All Reporters
```hocon
metrics {
  reporters = [
    {
      type: <type>
      durationUnit: "milliseconds"
      rateUnit: "seconds"
      frequency: "1 minute"
    }
  ]
}
```

| Name         | Default      | Description                                                           |
| :---         | :---         | :---                                                                  |
| durationUnit | milliseconds | The unit to report durations as. Overrides per-metric duration units. |
| rateUnit     | seconds      | The unit to report rates as. Overrides per-metric rate units.         |
| frequency    | 1 minute     | The frequency to report metrics. Overrides the default.               |

##### Console Reporter

```hocon
metrics {
  reporters = [
    {
      type: "console"
      timeZone: "UTC"
      output: "stdout"
    }
  ]
}
```

| Name     | Default | Description                                          |
| :---     | :---    | :---                                                 |
| timeZone | UTC     | The timezone to display dates/times for.             |
| output   | stdout  | The stream to write to. One of `stdout` or `stderr`. |

##### Graphite Reporter

```hocon
metrics {
  reporters = [
    {
      type: "graphite"
      host: "localhost"
      port: "8080"
      prefix: <prefix>
      sender: <sender>
    }
  ]
}
```

| Name     | Default   | Description                                            |
| :---     | :---      | :---                                                   |
| host     | localhost | The hostname of the Graphite server to report to.      |
| port     | 8080      | The stream to write to. One of stdout or stderr.       |
| prefix   | (none)    | The prefix for Metric key names to report to Graphite. |
| sender   | graphite  | Sender that is used to send metrics to Graphite.       |

##### Ganglia Reporter

```hocon
metrics {
  reporters = [
    {
      type: "ganglia"
      host: localhost
      port: 8649
      mode: unicast
      ttl: 1
      uuid: (none)
      spoof: "localhost:8649"
      tmax: 60
      dmax: 0
      prefix: <prefix>
    }
  ]
}
```

| Name     | Default   | Description                                                                                                |
| :---     | :---      | :---                                                                                                       |
| host     | localhost | The hostname (or group) of the Ganglia server(s) to report to.                                             |
| port     | 8649      | The port of the Ganglia server(s) to report to.                                                            |
| mode     | unicast   | The UDP addressing mode to announce the metrics with. One of unicast or multicast.                         |
| ttl      | 1         | The time-to-live of the UDP packets for the announced metrics.                                             |
| uuid     | (none)    | The UUID to tag announced metrics with.                                                                    |
| spoof    | (none)    | The hostname and port to use instead of this nodes for the announced metrics. In the format hostname:port. |
| tmax     | 60        | The tmax value to announce metrics with.                                                                   |
| dmax     | 0         | The dmax value to announce metrics with.                                                                   |
| prefix   | (none)    | The prefix for Metric key names to report to Graphite.                                                     |

### TODO

* Add more reporters
* Better Future support
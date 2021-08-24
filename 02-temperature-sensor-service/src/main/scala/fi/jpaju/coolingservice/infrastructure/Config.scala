package fi.jpaju.coolingservice
package infrastructure

import scala.concurrent.duration.Duration

import pureconfig.*
import pureconfig.error.*
import pureconfig.generic.derivation.default.*

import zio.*

case class Config(
    temperatureProducer: TemperatureProducerConfig
  ) derives ConfigReader

case class TemperatureProducerConfig(updateInterval: Duration, maxParallelism: Int)

object Config:
  def load: Task[Config] = Task.fromEither(
    ConfigSource
      .default
      .load[Config]
      .left
      .map(ConfigReaderException[Config](_))
  )

  lazy val layer = ZLayer.fromEffect(load)

package fi.jpaju
package coolingservice
package service

import fi.jpaju.coolingservice.domain.*
import fi.jpaju.coolingservice.event.*
import fi.jpaju.coolingservice.infrastructure.{ Logging, TemperatureProducerConfig }
import fi.jpaju.coolingservice.service.{ SensorPersistence, SensorPersistenceIM }

import zio.*
import zio.duration.*
import zio.clock.*
import zio.stream.ZStream

trait TemperatureStream:
  def stream: ZStream[Any, Nothing, TemperatureMeasured]

object TemperatureStream:
  lazy val stream: URIO[Has[TemperatureStream], ZStream[Any, Nothing, TemperatureMeasured]] =
    ZIO.serviceWith((s: TemperatureStream) => UIO(s.stream))

case class TemperatureStreamLive(
    persistence: SensorPersistence,
    client: TemperatureClient,
    config: TemperatureProducerConfig,
    logging: Logging,
    clock: Clock.Service
  ) extends TemperatureStream:

  override lazy val stream: ZStream[Any, Nothing, TemperatureMeasured] =
    val interval = Duration.fromScala(config.updateInterval)
    val tick     = ZStream.succeed(1L) ++ ZStream.fromSchedule(Schedule.spaced(interval))
    val hosts    = ZStream.fromIterableM(persistence.getAll)

    (tick *> hosts)
      .mapMParUnordered(config.maxParallelism)(fetchTemperetureFromHost)
      .flattenIterables
      .provide(Has(clock))

  private def fetchTemperetureFromHost(host: SensorHost): ZIO[Any, Nothing, List[TemperatureMeasured]] =
    client
      .getTemperatures(host.url.stringValue)
      .tapError(err => logging.error(s"Temperature request from host: $host, failed with: $err"))
      .orElseSucceed(List.empty)

end TemperatureStreamLive

object TemperatureStreamLive:
  type Dependencies = Has[SensorPersistence] & Has[TemperatureClient] & Has[TemperatureProducerConfig] & Has[Logging] &
    Clock

  lazy val layer: URLayer[Dependencies, Has[TemperatureStream]] =
    TemperatureStreamLive.apply.toLayer

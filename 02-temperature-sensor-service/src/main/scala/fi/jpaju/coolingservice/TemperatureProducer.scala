package fi.jpaju
package coolingservice

import fi.jpaju.coolingservice.infrastructure.*
import fi.jpaju.coolingservice.service.*

import org.http4s.client.Client

import zio.*
import zio.clock.*
import zio.console.*

object TemperatureProducer:

  val program: ZIO[Has[TemperatureStream] & Console, Nothing, Unit] =
    for
      temperatures <- TemperatureStream.stream
      _ <- temperatures
        .tap(temp => console.putStrLn(s"Temperature: $temp emitted").orDie)
        .runDrain
    yield ()

  def start: ZIO[ZEnv, Nothing, Unit] =

    val baseLayer = (Clock.live ++ Console.live) >+> ConsoleLogger.layer ++ Config.layer.orDie

    val persistenceLayer = SensorPersistenceIM.layer

    val temperatureClientLayer =
      ((baseLayer ++ HttpClientLive.layer) >>> TemperatureClientLive.layer).orDie

    val temperatureLayer: ZLayer[Clock & Console, Nothing, Has[TemperatureStream]] =
      (baseLayer >+> persistenceLayer ++ temperatureClientLayer ++ ZLayer.fromService((_: Config).temperatureProducer))
        >>> TemperatureStreamLive.layer

    program.provideCustomLayer(temperatureLayer)

  end start
end TemperatureProducer

package fi.jpaju
package coolingservice
package service

import java.time.OffsetDateTime

import fi.jpaju.coolingservice.domain.*
import fi.jpaju.coolingservice.event.*
import fi.jpaju.coolingservice.infrastructure.Logging

import io.circe.Codec

import org.http4s.*
import org.http4s.client.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

import zio.*
import zio.clock.*
import zio.interop.catz.*

enum TemperatureFetchError:
  case HostUnreachable(host: String)
  case CommunicationError(url: String, statusCode: Int)
  case InvalidResponse
  case Unknown

trait TemperatureClient:
  def getTemperatures(url: String): IO[TemperatureFetchError, List[TemperatureMeasured]]

object TemperatureClient:
  def getTemperatures(url: String): ZIO[Has[TemperatureClient], TemperatureFetchError, List[TemperatureMeasured]] =
    ZIO.serviceWith[TemperatureClient](_.getTemperatures(url))

case class TemperatureClientLive(
    client: Client[Task],
    clock: Clock.Service
  ) extends TemperatureClient:
  import TemperatureClientLive.SensorReading

  override def getTemperatures(url: String): IO[TemperatureFetchError, List[TemperatureMeasured]] =
    for
      readings <- client
        .expect[List[SensorReading]](url)
        .mapError(handleError)
      time <- clock.currentDateTime.orDie
    yield readings.map(reading => readingToMeasurement(reading, time))

  private def readingToMeasurement(reading: SensorReading, time: OffsetDateTime): TemperatureMeasured =
    TemperatureMeasured(
      sensorId = reading.id,
      temperature = Temperature.fromCelcius(reading.temperature),
      time = time
    )

  private def handleError(err: Throwable): TemperatureFetchError = err match
    case cf: ConnectionFailure => TemperatureFetchError.HostUnreachable(cf.upstream.getHostString.nn)
    case us: UnexpectedStatus  => TemperatureFetchError.CommunicationError(us.requestUri.renderString, us.status.code)
    case _: DecodeFailure      => TemperatureFetchError.InvalidResponse
    case other                 => TemperatureFetchError.Unknown

end TemperatureClientLive

object TemperatureClientLive:

  lazy val layer: RLayer[Has[Client[Task]] & Clock, Has[TemperatureClient]] =
    TemperatureClientLive.apply.toLayer

  private case class SensorReading(id: String, temperature: Float) derives Codec.AsObject
  private object SensorReading:
    import scala.language.unsafeNulls

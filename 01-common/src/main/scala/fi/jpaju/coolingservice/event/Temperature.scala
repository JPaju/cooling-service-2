package fi.jpaju
package coolingservice
package event

import java.time.OffsetDateTime

opaque type Temperature = Float

object Temperature:
  def fromCelcius(celcius: Float): Temperature =
    celcius

case class TemperatureMeasured(
  sensorId: String,
  temperature: Temperature,
  time: OffsetDateTime
)

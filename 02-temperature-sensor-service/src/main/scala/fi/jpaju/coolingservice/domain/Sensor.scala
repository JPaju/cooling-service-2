package fi.jpaju.coolingservice
package domain

import java.util.UUID

import scala.util.Try

opaque type SensorURL = String

object SensorURL:

  extension (url: SensorURL) def stringValue: String = url

  def fromString(host: String): Option[SensorURL] =
    Try(new java.net.URL(s"$host/temperatures"))
      .toOption
      .filter(_.getProtocol.toOption.exists(_.startsWith("http")))
      .flatMap(_.toString.toOption)

case class SensorHost(id: UUID, url: SensorURL)

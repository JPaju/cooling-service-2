package fi.jpaju.coolingservice
package service

import fi.jpaju.coolingservice.domain.*

import java.util.UUID

import zio.*
import scala.util.Try

trait SensorPersistence:
  def getAll: UIO[List[SensorHost]]

object SensorPersistence:
  def getAll: URIO[Has[SensorPersistence], List[SensorHost]] =
    ZIO.serviceWith[SensorPersistence](_.getAll)

case class InMemorySensorPersistence(sensors: List[SensorHost]) extends SensorPersistence:
  override def getAll: UIO[List[SensorHost]] = UIO.succeed(sensors)

object SensorPersistenceIM:
  private lazy val sensorHosts = List(1, 2)
    .map(n => s"http://jp-tempfan$n.int.jpaju.fi")
    .map(SensorURL.fromString)
    .collect { case Some(url) => SensorHost(java.util.UUID.randomUUID.nn, url) }

  def layer: ULayer[Has[SensorPersistence]] =
    ZLayer.succeed(InMemorySensorPersistence(sensorHosts))

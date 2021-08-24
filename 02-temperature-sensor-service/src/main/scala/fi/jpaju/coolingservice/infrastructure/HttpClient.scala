package fi.jpaju.coolingservice
package infrastructure

import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import zio.*
import zio.interop.catz.*

object HttpClientLive:
  lazy val layer: TaskLayer[Has[Client[Task]]] =
    given runtime: Runtime[ZEnv] = Runtime.default
    val client                   = BlazeClientBuilder[Task](runtime.platform.executor.asEC).resource
    ZLayer.fromManaged(client.toManagedZIO)

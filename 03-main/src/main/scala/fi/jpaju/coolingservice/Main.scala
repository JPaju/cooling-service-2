package fi.jpaju
package coolingservice

import zio.*
import zio.clock.*
import zio.console.*

object Main extends App:

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (for _ <- TemperatureProducer.start
    yield ()).exitCode

//    learn Scala 3, ZIO and Kafka

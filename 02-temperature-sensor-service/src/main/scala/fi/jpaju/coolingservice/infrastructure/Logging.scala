package fi.jpaju.coolingservice
package infrastructure

import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime

import scala.Console as SConsole
import scala.collection.immutable.Stream.Cons

import zio.*
import zio.clock.Clock
import zio.console.*

trait Logging:
  def info(msg: => String): UIO[Unit]
  def warning(msg: => String): UIO[Unit]
  def error(msg: => String): UIO[Unit]
  def error(msg: => String, t: Throwable): UIO[Unit]

object Logging:
  def info(msg: => String): URIO[Has[Logging], Unit] = ZIO.serviceWith[Logging](_.info(msg))

  def warning(msg: => String): URIO[Has[Logging], Unit] = ZIO.serviceWith[Logging](_.warning(msg))

  def error(msg: => String): URIO[Has[Logging], Unit] = ZIO.serviceWith[Logging](_.error(msg))

  def error(msg: => String, t: Throwable): URIO[Has[Logging], Unit] = ZIO.serviceWith[Logging](_.error(msg, t))

case class ConsoleLogger(
    console: Console.Service,
    clock: Clock.Service
  ) extends Logging:
  private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").nn

  override def info(msg: => String): UIO[Unit] =
    log(createMsg(msg, "info"))

  override def warning(msg: => String): UIO[Unit] =
    log(createMsg(msg, "warning"))

  override def error(msg: => String): UIO[Unit] =
    log(createMsg(msg, "error"))

  override def error(msg: => String, t: Throwable): UIO[Unit] =
    log(createMsg(msg, "error")) *> ZIO.effect(t.printStackTrace).orDie

  private type LogLevel = "info" | "warning" | "error"

  private def createMsg(msg: String, level: LogLevel): String =
    val prefix = color(s"[$level]", level)
    s"$prefix: $msg"

  private def color(str: String, level: LogLevel): String =
    level match
      case "info"    => SConsole.GREEN ++ str ++ SConsole.RESET
      case "warning" => SConsole.YELLOW ++ str ++ SConsole.RESET
      case "error"   => SConsole.RED ++ str ++ SConsole.RESET

  private def log(msg: String): UIO[Unit] = for
    time <- clock.currentDateTime.orDie
    formattedTime = time.format(timeFormatter)
    toLog         = s"[$formattedTime] $msg"
    _ <- console.putStrLn(toLog).orDie
  yield ()

object ConsoleLogger:
  lazy val layer: URLayer[Console & Clock, Has[Logging]] = (ConsoleLogger(_, _)).toLayer

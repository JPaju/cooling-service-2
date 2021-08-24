import sbt._

object Dependencies {
  private object Version {
    val circe          = "0.14.1"
    val http4s         = "1.0.0-M22"
    val pureconfig     = "0.16.0"
    val zio            = "1.0.10"
    val zioInteropCats = "3.1.1.0"
  }

  val circeCore    = "io.circe" %% "circe-core"    % Version.circe
  val circeGeneric = "io.circe" %% "circe-generic" % Version.circe
  val circeParser  = "io.circe" %% "circe-parser"  % Version.circe

  val http4sDsl    = "org.http4s" %% "http4s-dsl"          % Version.http4s
  val http4sClient = "org.http4s" %% "http4s-blaze-client" % Version.http4s
  val http4sCirce  = "org.http4s" %% "http4s-circe"        % Version.http4s

  val pureconfigCore = "com.github.pureconfig" %% "pureconfig-core" % Version.pureconfig

  val zio            = "dev.zio" %% "zio"              % Version.zio
  val zioStreams     = "dev.zio" %% "zio-streams"      % Version.zio
  val zioInteropCats = "dev.zio" %% "zio-interop-cats" % Version.zioInteropCats
}

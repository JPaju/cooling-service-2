import Dependencies._

ThisBuild / organization := "fi.jpaju"
ThisBuild / scalaVersion := "3.0.1"

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Ykind-projector",
    "-Yexplicit-nulls",
    "-Ysafe-init"
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future")

lazy val `cooling-service` =
  project
    .in(file("."))
    .settings(name := "cooling-service")
    .aggregate(
      common,
      `temperature-sensor-service`,
      main
    )

lazy val common =
  project
    .in(file("01-common"))
    .settings(commonSettings)

lazy val `temperature-sensor-service` =
  project
    .in(file("02-temperature-sensor-service"))
    .dependsOn(common)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        http4sDsl,
        http4sClient,
        http4sCirce,
        circeCore,
        circeGeneric,
        circeParser,
        pureconfigCore,
        zio,
        zioStreams,
        zioInteropCats
      )
    )

lazy val main =
  project
    .in(file("03-main"))
    .settings(commonSettings)
    .dependsOn(`temperature-sensor-service`)

lazy val commonSettings = Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty,
  Compile / console / scalacOptions --= Seq(
    "-Wunused:_",
    "-Xfatal-warnings"
  ),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value
)

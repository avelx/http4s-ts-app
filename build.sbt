val http4sVersion = "0.23.30"
val circeVersion  = "0.14.10"

ThisBuild / scalaVersion := "3.3.5"
ThisBuild / organization := "example"

lazy val root = (project in file("."))
  .settings(
    name := "http4s-ts-app",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"         % "3.5.7",
      "org.http4s"    %% "http4s-ember-server" % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "io.circe"      %% "circe-generic"       % circeVersion,
      "ch.qos.logback" % "logback-classic"     % "1.5.16" % Runtime
    ),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    run / fork := true
  )

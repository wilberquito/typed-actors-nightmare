ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.0"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-template",
    idePackagePrefix := Some("edu.udg.pda")
  )

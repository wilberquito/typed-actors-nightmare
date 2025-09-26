ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6" // Uses LTS scala

lazy val root = (project in file("."))
  .settings(
    name := "sbt-template",
    idePackagePrefix := Some("edu.udg.pda"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.1",    // Akka actor
      "org.slf4j" % "slf4j-simple" % "1.6.4"  // Logging framework
    )
  )

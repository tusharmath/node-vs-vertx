import Dependencies._

// cancelable in Global := true
// fork in run := true

// ThisBuild / fork := false
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalacOptions := Seq(
  "-language:postfixOps"
)
mainClass in (Compile, run) := Some("zion.MHello")
lazy val root = (project in file("."))
  .settings(
    name := "zion-scala"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

libraryDependencies += "dev.zio" %% "zio-nio" % "0.4.0"

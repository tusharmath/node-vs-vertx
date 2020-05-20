// cancelable in Global := true
// fork in run := true

// ThisBuild / fork := false
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalacOptions := Seq(
  "-language:postfixOps"
)
mainClass in (Compile, run) := Some("zion.HelloZIO")

libraryDependencies += "dev.zio"  %% "zio"         % "1.0.0-RC19-2"
libraryDependencies += "io.netty" % "netty-all"    % "4.1.49.Final"
libraryDependencies += "dev.zio"  %% "zio-streams" % "1.0.0-RC19-2"

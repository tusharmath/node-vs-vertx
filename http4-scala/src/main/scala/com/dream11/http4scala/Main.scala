package com.dream11.http4scala

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    Http4scalaServer.stream[IO].compile.drain.as(ExitCode.Success)
}
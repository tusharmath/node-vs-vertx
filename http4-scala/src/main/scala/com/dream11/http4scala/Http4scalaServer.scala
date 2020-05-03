package com.dream11.http4scala

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import scala.concurrent.ExecutionContext.global

object Http4scalaServer {

  def stream[F[_]: ConcurrentEffect](
      implicit T: Timer[F],
      C: ContextShift[F]
  ): Stream[F, Nothing] = {

    BlazeServerBuilder[F]
      .bindHttp(5000, "0.0.0.0")
      .withHttpApp(Http4scalaRoutes.helloWorldRoutes[F].orNotFound)
      .serve
  }.drain
}

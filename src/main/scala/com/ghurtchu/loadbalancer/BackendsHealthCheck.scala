package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.RefWrapper.HealthChecks

import scala.concurrent.duration.DurationInt

object BackendsHealthCheck {

  def periodically(
    healthChecks: HealthChecks,
    parseUri: ParseUri,
    backendFromHealthCheck: String => String,
    updateBackends: UpdateBackends,
    roundRobin: RoundRobin,
    send: Send[ServerStatus],
  ): IO[Unit] =
    (for {
      current <- roundRobin(healthChecks)
      _       <- IO.println(s"Checking health status of $current")
      uri     <- IO.fromEither(parseUri(current))
      status  <- send(uri)
      _       <- updateBackends(status, backendFromHealthCheck(current))
    } yield ())
      .flatMap(_ => IO.sleep(2500.millis))
      .foreverM
}

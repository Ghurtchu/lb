package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.WrappedRef.{Backends, HealthChecks}

import scala.concurrent.duration.DurationInt

object HealthCheckBackends {

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    backendFromHealthCheck: String => String,
    updateWrappedRefUrls: UpdateWrappedRefUrls,
    roundRobin: RoundRobin,
    send: Send[ServerStatus],
  ): IO[Unit] =
    (for {
      current <- roundRobin(healthChecks)
      _       <- IO.println(s"Checking availability of $current")
      uri     <- IO.fromEither(parseUri(current))
      status  <- send(uri)
      _       <- updateWrappedRefUrls(
        backends,
        status,
        backendFromHealthCheck(current),
      )
    } yield ())
      .flatMap(_ => IO.sleep(2500.millis))
      .foreverM
}

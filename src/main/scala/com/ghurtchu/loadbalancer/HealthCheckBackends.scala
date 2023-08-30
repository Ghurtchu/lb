package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Server.ServerStatus
import com.ghurtchu.loadbalancer.UrlsRef.{Backends, HealthChecks}

import scala.concurrent.duration.DurationInt

object HealthCheckBackends {

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateRefUrls: UpdateRefUrlsAndGet,
    roundRobin: RoundRobin,
    sendAndExpectServerStatus: SendAndExpect[ServerStatus],
  ): IO[Unit] =
    (for {
      current <- roundRobin(healthChecks)
      uri     <- IO.fromEither(parseUri(current.value))
      status  <- sendAndExpectServerStatus(uri)
      _       <- updateRefUrls(backends, current, status)
    } yield ())
      .flatMap(_ => IO.sleep(2500.millis))
      .foreverM
}

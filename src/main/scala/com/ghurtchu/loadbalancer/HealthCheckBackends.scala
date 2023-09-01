package com.ghurtchu.loadbalancer

import cats.Id
import cats.effect.IO
import com.ghurtchu.loadbalancer.HttpServer.Status
import com.ghurtchu.loadbalancer.UrlsRef.{Backends, HealthChecks}

import scala.concurrent.duration.DurationInt

object HealthCheckBackends {

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateRef: UpdateRefUrlsAndGet,
    roundRobin: RoundRobin[Id],
    sendAndExpectServerStatus: SendAndExpect[Status],
  ): IO[Unit] =
    (for {
      current <- roundRobin(healthChecks)
      uri     <- IO.fromEither(parseUri(current.value))
      status  <- sendAndExpectServerStatus(uri)
      _       <- updateRef(backends, current, status)
    } yield ())
      .flatMap(_ => IO.sleep(2500.millis))
      .foreverM
}

package com.ghurtchu.loadbalancer.services

import cats.Id
import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.UrlsRef.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.http.HttpServer.Status

import scala.concurrent.duration.DurationLong

object HealthCheckBackends {

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateRef: UpdateRefUrlsAndGet,
    roundRobin: RoundRobin[Id],
    sendAndExpectServerStatus: SendAndExpect[Status],
    healthCheckInterval: Long,
  ): IO[Unit] =
    (for {
      current <- roundRobin(healthChecks)
      uri     <- IO.fromEither(parseUri(current.value))
      status  <- sendAndExpectServerStatus(uri)
      _       <- updateRef(backends, current, status)
    } yield ())
      .flatMap(_ => IO.sleep(healthCheckInterval.seconds))
      .foreverM
}

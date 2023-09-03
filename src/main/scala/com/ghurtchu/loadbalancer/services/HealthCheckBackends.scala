package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.UrlsRef.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.http.HttpServer.Status
import com.ghurtchu.loadbalancer.services.RoundRobin.HealthChecksRoundRobin

import scala.concurrent.duration.DurationLong

object HealthCheckBackends {

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateRef: UpdateRefUrlsAndGet,
    healthChecksRoundRobin: HealthChecksRoundRobin,
    sendAndExpectStatus: SendAndExpect[Status],
    healthCheckInterval: Long,
  ): IO[Unit] =
    (for {
      currentUrl <- healthChecksRoundRobin(healthChecks)
      uri        <- IO.fromEither(parseUri(currentUrl.value))
      status     <- sendAndExpectStatus(uri)
      ref        <- updateRef(backends, currentUrl, status)
    } yield ref)
      .flatMap(_ => IO.sleep(healthCheckInterval.seconds))
      .foreverM
}

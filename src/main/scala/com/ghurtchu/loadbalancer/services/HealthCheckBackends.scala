package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.*
import com.ghurtchu.loadbalancer.domain.UrlsRef.*
import com.ghurtchu.loadbalancer.http.ServerHealthStatus
import com.ghurtchu.loadbalancer.services.RoundRobin.HealthChecksRoundRobin

import scala.concurrent.duration.DurationLong
object HealthCheckBackends:

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateBackendsAndGet: UpdateBackendsAndGet,
    healthChecksRoundRobin: HealthChecksRoundRobin,
    sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
    healthCheckInterval: HealthCheckInterval,
  ): IO[Unit] =
    checkHealthAndUpdateBackends(
      healthChecks,
      backends,
      parseUri,
      updateBackendsAndGet,
      healthChecksRoundRobin,
      sendAndExpectStatus,
    ).flatMap(_ => IO.sleep(healthCheckInterval.value.seconds)).foreverM

  private[services] def checkHealthAndUpdateBackends(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateBackendsAndGet: UpdateBackendsAndGet,
    healthChecksRoundRobin: HealthChecksRoundRobin,
    sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
  ): IO[Urls] =
    for
      currentUrl <- healthChecksRoundRobin(healthChecks)
      uri        <- IO.fromEither(parseUri(currentUrl.value))
      status     <- sendAndExpectStatus(uri)
      updated    <- updateBackendsAndGet(backends, currentUrl, status)
    yield updated

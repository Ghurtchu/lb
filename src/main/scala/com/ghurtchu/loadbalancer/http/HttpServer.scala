package com.ghurtchu.loadbalancer.http

import cats.Id
import cats.effect.IO
import com.comcast.ip4s._
import com.ghurtchu.loadbalancer.domain.UrlsRef.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.services.{
  HealthCheckBackends,
  LoadBalancer,
  ParseUri,
  RoundRobin,
  SendAndExpect,
  UpdateRefUrlsAndGet,
}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object HttpServer {

  sealed trait Status

  object Status {
    case object Alive extends Status
    case object Dead  extends Status
  }

  def start(
    backends: Backends,
    healthChecks: HealthChecks,
    port: Port,
    host: Host,
    healthCheckInterval: Long,
    parseUri: ParseUri,
    updateAndGet: UpdateRefUrlsAndGet,
    backendsRoundRobin: RoundRobin[Option],
    healthChecksRoundRobin: RoundRobin[Id],
  ): IO[Unit] =
    (for {
      client <- EmberClientBuilder
        .default[IO]
        .build
      httpApp = Logger
        .httpApp(logHeaders = true, logBody = true) {
          LoadBalancer
            .from(
              backends,
              SendAndExpect.toBackend(HttpClient.of(client), _),
              parseUri,
              backendsRoundRobin,
            )
            .orNotFound
        }
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(httpApp)
        .build
      _ <- HealthCheckBackends
        .periodically(
          healthChecks,
          backends,
          parseUri,
          updateAndGet,
          healthChecksRoundRobin,
          SendAndExpect.toHealthCheck(HttpClient.of(client)),
          healthCheckInterval,
        )
        .toResource
    } yield ()).useForever
}
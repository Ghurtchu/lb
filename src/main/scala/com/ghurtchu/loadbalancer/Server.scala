package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.comcast.ip4s._
import com.ghurtchu.loadbalancer.UrlsRef.{Backends, HealthChecks}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object Server {

  sealed trait ServerStatus

  object ServerStatus {
    case object Alive extends ServerStatus
    case object Dead  extends ServerStatus
  }

  def start(
    backends: Backends,
    healthChecks: HealthChecks,
    port: Port,
    host: Host,
    parseUri: ParseUri,
    updateAndGet: UpdateRefUrlsAndGet,
    roundRobin: RoundRobin,
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
              SendAndExpect.toBackend(client, _),
              parseUri,
              roundRobin,
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
          roundRobin,
          SendAndExpect.toHealthCheck(client),
        )
        .toResource
    } yield ()).useForever
}

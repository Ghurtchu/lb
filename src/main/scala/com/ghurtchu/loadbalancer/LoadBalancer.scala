package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.comcast.ip4s._
import com.ghurtchu.loadbalancer.Urls.{Backends, HealthChecks}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object LoadBalancer {

  def run(
    backends: Backends,
    healthChecks: HealthChecks,
    port: Port,
    host: Host,
    config: Config,
  ): IO[Unit] = {
    for {
      client <- EmberClientBuilder
        .default[IO]
        .build
      httpApp = Logger.httpApp(
        logHeaders = true,
        logBody = true,
      ) {
        Routes
          .from(backends, client)
          .orNotFound
      }
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(httpApp)
        .build
      _ <- BackendsHealthCheck
        .periodically(backends, healthChecks, client, config)
        .toResource
    } yield ()
  }.useForever
}

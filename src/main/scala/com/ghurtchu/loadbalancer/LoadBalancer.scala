package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.comcast.ip4s._
import com.ghurtchu.loadbalancer.Urls.RefWrapper.{Backends, HealthChecks}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object LoadBalancer {

  def run(
    backends: Backends,
    healthChecks: HealthChecks,
    port: Port,
    host: Host,
    backendFromHealthCheck: String => String,
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
          .from(backends, ForwardRequest.of(client), ParseUri.of, RoundRobin.of)
          .orNotFound
      }
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(httpApp)
        .build
      _ <- BackendsHealthCheck
        .periodically(
          healthChecks,
          client,
          ParseUri.of,
          backendFromHealthCheck,
          UpdateBackends.of(backends),
          RoundRobin.of,
        )
        .toResource
    } yield ()
  }.useForever
}

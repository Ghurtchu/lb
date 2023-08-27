package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}
import com.comcast.ip4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object LoadbalancerServer {

  def run(backends: Ref[IO, Backends], port: Port, host: Host): IO[Unit] = {
    for {
      client <- EmberClientBuilder.default[IO].build
      httpApp = Logger.httpApp(logHeaders = true, logBody = true) {
        LoadbalancerRoutes.routes(backends, client).orNotFound
      }
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(httpApp)
        .build
    } yield ()
  }.useForever
}

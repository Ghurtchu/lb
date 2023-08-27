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
      httpApp = LoadbalancerRoutes.routes(backends, client).orNotFound
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(finalHttpApp)
        .build
    } yield ()
  }.useForever
}

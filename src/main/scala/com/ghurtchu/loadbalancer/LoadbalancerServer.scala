package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}
import cats.implicits.toSemigroupKOps
import com.comcast.ip4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object LoadbalancerServer {

  def run(backends: Ref[IO, Backends], port: Port): IO[Unit] = {
    for {
      client <- EmberClientBuilder.default[IO].build
      httpApp =
          (LoadbalancerRoutes.requestRoutes(backends, client) <+>
            LoadbalancerRoutes.helloRoutes(ipv4"0.0.0.0".toString concat "/" concat s"${port.value}"))
            .orNotFound
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
      _ <-
        EmberServerBuilder.default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port)
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}

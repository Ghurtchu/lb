package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.client.Client

object LoadbalancerRoutes {

  def requestRoutes(backends: Ref[IO, Backends], client: Client[IO]): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] {
      case request @ GET -> Root =>
        for {
          backend <- backends.getAndUpdate(_.next)
          current = backend.current
          uri <- IO.fromOption((Uri fromString current).toOption) {
            new RuntimeException("Could not construct proper URI")
          }
          response <- client.expect[String](request.withUri(uri))
            .recover(_ => s"server with uri: $uri is dead")
          resp <- Ok(response)
        } yield resp
    }
  }

  def helloRoutes(uri: String): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] {
      case GET -> Root / "hello" => Ok(s"Hello from $uri")
    }
  }

}
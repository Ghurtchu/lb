package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.client.Client

object LoadbalancerRoutes {

  def routes(
    backends: Ref[IO, Urls],
    client: Client[IO],
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case request @ GET -> Root =>
      for {
        current  <- backends.getAndUpdate(_.next).map(_.current)
        uri      <- IO.fromOption(Uri.fromString(current).toOption) {
          new RuntimeException("Could not construct proper URI")
        }
        response <- client
          .expect[String](request.withUri(uri))
          .recover(_ => s"server with uri: $uri is dead")
        resp     <- Ok(response)
      } yield resp
    }
  }
}

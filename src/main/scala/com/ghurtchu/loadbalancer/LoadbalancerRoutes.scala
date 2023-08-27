package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.Backends
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.client.Client

object LoadbalancerRoutes {

  def routes(
    backends: Backends,
    client: Client[IO],
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case request @ GET -> Root =>
      for {
        current  <- backends.ref
          .getAndUpdate(_.next)
          .map(_.current)
        uri      <- IO.fromOption {
          Uri.fromString(current).toOption
        }(new RuntimeException("Could not construct proper URI"))
        response <- client
          .expect[String](request.withUri(uri))
          .handleError(_ => s"server with uri: $uri is dead")
        result   <- Ok(response)
      } yield result
    }
  }
}

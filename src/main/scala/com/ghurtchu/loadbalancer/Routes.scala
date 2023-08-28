package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.Backends
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.client.Client

object Routes {

  def from(
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
          Uri
            .fromString(current)
            .toOption
        }(InvalidURI(current))
        response <- client
          .expect[String](request.withUri(uri))
          .handleError(_ => s"server with uri: $uri is dead")
        result   <- Ok(response)
      } yield result
    }
  }

  final private case class InvalidURI(uri: String) extends Throwable {
    override def getMessage: String =
      s"Could not construct proper URI from $uri"
  }
}

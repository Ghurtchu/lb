package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.UrlsRef.Backends
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request}

object LoadBalancer {

  def from(
    backends: Backends,
    send: Request[IO] => SendAndExpect[String],
    parseUri: ParseUri,
    roundRobin: RoundRobin[Option],
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case req @ GET -> Root =>
      roundRobin(backends).flatMap {
        _.fold(Ok("All backends are inactive")) { backend =>
          for {
            uri      <- IO.fromEither(parseUri(backend.value))
            response <- send(req)(uri)
            result   <- Ok(response)
          } yield result
        }
      }
    }
  }

  final case class InvalidUri(uri: String) extends Throwable {
    override def getMessage: String =
      s"Could not construct proper URI from $uri"
  }
}

package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.UrlsRef.Backends
import org.http4s.{HttpRoutes, Request}
import org.http4s.dsl.Http4sDsl

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
      for {
        current <- roundRobin(backends)
        result  <- current.fold(Ok("All backends are inactive")) { backend =>
          for {
            uri      <- IO.fromEither(parseUri(backend.value))
            response <- send(req)(uri)
            result   <- Ok(response)
          } yield result
        }
      } yield result
    }
  }

  final case class InvalidUri(uri: String) extends Throwable {
    override def getMessage: String =
      s"Could not construct proper URI from $uri"
  }
}

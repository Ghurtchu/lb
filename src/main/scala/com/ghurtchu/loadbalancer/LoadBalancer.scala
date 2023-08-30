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
    roundRobin: RoundRobin,
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case request @ GET -> Root =>
      for {
        current  <- roundRobin(backends)
        uri      <- IO.fromEither(parseUri(current.value))
        response <- send(request)(uri)
        result   <- Ok(response)
      } yield result
    }
  }

  final case class InvalidUri(uri: String) extends Throwable {
    override def getMessage: String =
      s"Could not construct proper URI from $uri"
  }
}

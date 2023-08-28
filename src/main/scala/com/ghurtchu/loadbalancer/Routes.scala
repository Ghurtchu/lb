package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.RefWrapper.Backends
import org.http4s.{HttpRoutes, Request}
import org.http4s.dsl.Http4sDsl

object Routes {

  def from(
    backends: Backends,
    send: Request[IO] => Send[String],
    parseUri: ParseUri,
    roundRobin: RoundRobin,
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case request @ GET -> Root =>
      for {
        current  <- roundRobin(backends)
        uri      <- IO.fromEither(parseUri(current))
        response <- send(request)(uri)
        result   <- Ok(response)
      } yield result
    }
  }

  final case class InvalidURI(uri: String) extends Throwable {
    override def getMessage: String =
      s"Could not construct proper URI from $uri"
  }
}

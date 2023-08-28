package com.ghurtchu.loadbalancer

import cats.effect.IO
import org.http4s.{Request, Uri}
import org.http4s.client.Client

trait ForwardRequest {
  def apply(uri: Uri, request: Request[IO]): IO[String]
}

object ForwardRequest {

  def of(client: Client[IO]): ForwardRequest = (uri, request) =>
    client
      .expect[String](request.withUri(uri))
      .handleError(_ => s"server with uri: $uri is dead")

  def test: ForwardRequest =
    (_, _) => IO("I am alive")
}

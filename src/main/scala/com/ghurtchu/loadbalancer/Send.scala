package com.ghurtchu.loadbalancer

import cats.effect.IO
import org.http4s.{Request, Uri}
import org.http4s.client.Client

import scala.concurrent.duration.DurationInt

trait Send[A] {
  def apply(uri: Uri): IO[A]
}

object Send {

  def toBackend(client: Client[IO], request: Request[IO]): Send[String] =
    uri =>
      client
        .expect[String](request.withUri(uri))
        .handleError(_ => s"server with uri: $uri is dead")

  def toHealthCheck(client: Client[IO]): Send[ServerStatus] =
    client
      .expect[String](_)
      .as(ServerStatus.Alive)
      .timeout(5.seconds)
      .handleError(_ => ServerStatus.Dead)
}

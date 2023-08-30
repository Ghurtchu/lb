package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Server.ServerStatus
import org.http4s.{Request, Uri}
import org.http4s.client.Client

import scala.concurrent.duration.DurationInt

trait SendAndExpect[+A] {
  def apply(uri: Uri): IO[A]
}

object SendAndExpect {

  def toBackend(client: Client[IO], request: Request[IO]): SendAndExpect[String] =
    uri =>
      client
        .expect[String](request.withUri(uri))
        .handleError(_ => s"server with uri: $uri is dead")

  def toHealthCheck(client: Client[IO]): SendAndExpect[ServerStatus] =
    client
      .expect[String](_)
      .as(ServerStatus.Alive)
      .timeout(5.seconds)
      .handleError(_ => ServerStatus.Dead)
}

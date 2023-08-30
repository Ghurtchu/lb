package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.HttpServer.Status
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

  def toHealthCheck(client: Client[IO]): SendAndExpect[Status] =
    client
      .expect[String](_)
      .as(Status.Alive)
      .timeout(5.seconds)
      .handleError(_ => Status.Dead)
}

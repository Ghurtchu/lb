package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import com.ghurtchu.loadbalancer.http.HttpClient
import com.ghurtchu.loadbalancer.http.HttpServer.Status
import org.http4s.{Request, Uri}

import scala.concurrent.duration.DurationInt

trait SendAndExpect[A] {
  def apply(uri: Uri): IO[A]
}

object SendAndExpect {

  def toBackend(httpClient: HttpClient, req: Request[IO]): SendAndExpect[String] = new SendAndExpect[String] {
    override def apply(uri: Uri): IO[String] =
      httpClient
        .sendAndReceive(uri, Some(req))
        .handleError(_ => s"server with uri: $uri is dead")
  }

  def toHealthCheck(httpClient: HttpClient): SendAndExpect[Status] = new SendAndExpect[Status] {
    override def apply(uri: Uri): IO[Status] =
      httpClient
        .sendAndReceive(uri)
        .as(Status.Alive)
        .timeout(5.seconds)
        .handleError(_ => Status.Dead)
  }

}

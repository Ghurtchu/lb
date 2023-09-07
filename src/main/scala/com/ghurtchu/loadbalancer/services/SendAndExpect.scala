package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import com.ghurtchu.loadbalancer.http.HttpClient
import com.ghurtchu.loadbalancer.http.HttpServer.Status
import org.http4s.client.UnexpectedStatus
import org.http4s.{Request, Uri}
import cats.syntax.option._

import scala.concurrent.duration.{DurationInt, NANOSECONDS}

trait SendAndExpect[A] {
  def apply(uri: Uri): IO[A]
}

object SendAndExpect {

  def toBackend(httpClient: HttpClient, req: Request[IO]): SendAndExpect[String] =
    new SendAndExpect[String] {
      override def apply(uri: Uri): IO[String] =
        httpClient
          .sendAndReceive(uri, req.some)
          .handleError {
            case _: UnexpectedStatus => s"resource at uri: [$uri] was not found"
            case _                   => s"server with uri: [$uri] is dead"
          }
    }

  def toHealthCheck(httpClient: HttpClient): SendAndExpect[Status] =
    new SendAndExpect[Status] {
      override def apply(uri: Uri): IO[Status] =
        httpClient
          .sendAndReceive(uri, none)
          .as(Status.Alive)
          .timeout(5.seconds)
          .handleError(_ => Status.Dead)
    }

}

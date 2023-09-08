package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.http.{HttpClient, ServerHealthStatus}
import org.http4s.client.UnexpectedStatus
import org.http4s.{Request, Uri}
import cats.syntax.option.*
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import cats.syntax.applicative._

import scala.concurrent.duration.DurationInt

trait SendAndExpect[A]:
  def apply(uri: Uri): IO[A]

object SendAndExpect:

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def toBackend(httpClient: HttpClient, req: Request[IO]): SendAndExpect[String] =
    new SendAndExpect[String]:
      override def apply(uri: Uri): IO[String] =
        info"[LOAD-BALANCER] sending request to $uri" *> httpClient
          .sendAndReceive(uri, req.some)
          .handleErrorWith {
            case _: UnexpectedStatus =>
              s"resource at uri: $uri was not found"
                .pure[IO]
                .flatTap(msg => warn"$msg")
            case _                   =>
              s"server with uri: $uri is dead"
                .pure[IO]
                .flatTap(msg => warn"$msg")
          }

  def toHealthCheck(httpClient: HttpClient): SendAndExpect[ServerHealthStatus] =
    new SendAndExpect[ServerHealthStatus]:
      override def apply(uri: Uri): IO[ServerHealthStatus] =
        info"[HEALTH-CHECK] checking $uri health" *>
          httpClient
            .sendAndReceive(uri, none)
            .as(ServerHealthStatus.Alive)
            .flatTap(ss => info"$uri is alive")
            .timeout(5.seconds)
            .handleErrorWith(_ => warn"$uri is dead" *> IO.pure(ServerHealthStatus.Dead))

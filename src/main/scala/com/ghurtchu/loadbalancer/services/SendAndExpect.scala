package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.http.{HttpClient, ServerStatus}
import org.http4s.client.UnexpectedStatus
import org.http4s.{Request, Uri}
import cats.syntax.option.*
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

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
              IO.pure(s"resource at uri: $uri was not found")
                .flatTap(msg => warn"$msg")
            case _                   =>
              IO.pure(s"server with uri: $uri is dead")
                .flatTap(msg => warn"$msg")
          }

  def toHealthCheck(httpClient: HttpClient): SendAndExpect[ServerStatus] =
    new SendAndExpect[ServerStatus]:
      override def apply(uri: Uri): IO[ServerStatus] =
        info"[HEALTH-CHECK] checking $uri health" *>
          httpClient
            .sendAndReceive(uri, none)
            .as(ServerStatus.Alive)
            .flatTap(ss => info"$uri is alive")
            .timeout(5.seconds)
            .handleErrorWith(_ => warn"$uri is dead" *> IO.pure(ServerStatus.Dead))

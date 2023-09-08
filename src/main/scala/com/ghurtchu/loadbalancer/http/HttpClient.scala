package com.ghurtchu.loadbalancer.http

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.{Request, Uri}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*

import scala.concurrent.duration.DurationInt

trait HttpClient:
  def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String]

object HttpClient:

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def of(client: Client[IO]): HttpClient = new HttpClient:
    override def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      requestOpt.fold(client.expect[String](uri)) { request =>
        client.expect[String](request.withUri(uri))
      }

  val testSuccess: HttpClient = (_, _) => IO.pure("Hello")
  val testFailure: HttpClient = (_, _) => IO.raiseError(new RuntimeException("Server is dead"))
  val testTimeout: HttpClient = (_, _) => IO.sleep(6.seconds).as("Hello")

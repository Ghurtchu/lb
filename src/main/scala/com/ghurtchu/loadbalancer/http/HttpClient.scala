package com.ghurtchu.loadbalancer.http

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.{Request, Uri}

import scala.concurrent.duration.DurationInt

trait HttpClient {
  def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String]
}

object HttpClient {

  def of(client: Client[IO]): HttpClient = new HttpClient {
    override def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      requestOpt.fold(
        client.expect[String](uri),
      ) { request =>
        client.expect[String](request.withUri(uri))
      }
  }

  val testSuccess: HttpClient = new HttpClient {
    override def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      IO.pure("Hello")
  }

  val testFailure: HttpClient = new HttpClient {
    override def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      IO.raiseError(new RuntimeException("Server is dead"))
  }

  val testTimeout: HttpClient = new HttpClient {
    override def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      IO.sleep(6.seconds)
        .as("Hello")
  }
}

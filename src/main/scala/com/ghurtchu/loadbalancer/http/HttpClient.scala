package com.ghurtchu.loadbalancer.http

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.{Request, Uri}

import scala.concurrent.duration.DurationInt

trait HttpClient {
  def send(uri: Uri, requestOpt: Option[Request[IO]]): IO[String]
}

object HttpClient {

  def of(client: Client[IO]): HttpClient = new HttpClient {
    override def send(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      requestOpt match {
        case Some(req) => client.expect[String](req.withUri(uri))
        case None      => client.expect[String](uri)
      }
  }

  val testSuccess: HttpClient = new HttpClient {
    override def send(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      IO.pure("Hello")
  }

  val testFailure: HttpClient = new HttpClient {
    override def send(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      IO.raiseError(new RuntimeException("Server is dead"))
  }

  val testTimeout: HttpClient = new HttpClient {
    override def send(uri: Uri, requestOpt: Option[Request[IO]]): IO[String] =
      IO.sleep(6.seconds)
        .as("Hello")
  }
}

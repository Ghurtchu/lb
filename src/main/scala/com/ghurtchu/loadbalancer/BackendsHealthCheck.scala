package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.{Backends, HealthChecks}
import org.http4s.Uri
import org.http4s.client.Client

import scala.concurrent.duration.DurationInt

object BackendsHealthCheck {

  def periodically(
    backends: Backends,
    healthChecks: HealthChecks,
    client: Client[IO],
    backendFromHealthCheck: String => String,
  ): IO[Unit] =
    (for {
      current <- healthChecks.ref
        .getAndUpdate(_.next)
        .map(_.current)
      _       <- IO.println(s"Checking health status of $current")
      uri     <- IO.fromEither(Uri.fromString(current))
      status  <- client
        .expect[String](uri)
        .as(ServerStatus.Alive)
        .timeout(5.seconds)
        .handleError(_ => ServerStatus.Dead)
      backend = backendFromHealthCheck(current)
      _ <- status match {
        case ServerStatus.Alive =>
          IO.println(s"$backend is alive") *>
            backends.ref
              .update(_.add(backend))
        case ServerStatus.Dead  =>
          IO.println(s"$backend is dead") *>
            backends.ref
              .update(_.remove(backend))
      }
    } yield ())
      .flatMap(_ => IO.sleep(2500.millis))
      .foreverM
}

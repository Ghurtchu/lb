package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.{Backends, HealthChecks}
import org.http4s.Uri
import org.http4s.client.Client

import scala.concurrent.duration.DurationInt

object BackendHealthChecks {
  def run(
    backends: Backends,
    healthChecks: HealthChecks,
    client: Client[IO],
    config: Config,
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
      backend = config.backendUrlFromHealthCheckUrl(current)
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
      .flatMap(_ =>
        IO.sleep(1200.millis),
      ) // health check each server per 1.2 seconds
      .foreverM
}

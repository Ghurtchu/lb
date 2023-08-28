package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.RefWrapper.HealthChecks
import org.http4s.client.Client

import scala.concurrent.duration.DurationInt

object BackendsHealthCheck {

  def periodically(
    healthChecks: HealthChecks,
    client: Client[IO],
    parseUri: ParseUri,
    backendFromHealthCheck: String => String,
    updateBackends: UpdateBackends,
    roundRobin: RoundRobin,
  ): IO[Unit] =
    (for {
      current <- roundRobin(healthChecks)
      _       <- IO.println(s"Checking health status of $current")
      uri     <- IO.fromEither(parseUri(current))
      status  <- client
        .expect[String](uri)
        .as(ServerStatus.Alive)
        .timeout(5.seconds)
        .handleError(_ => ServerStatus.Dead)
      _       <- updateBackends(status, backendFromHealthCheck(current))
    } yield ())
      .flatMap(_ => IO.sleep(2500.millis))
      .foreverM
}

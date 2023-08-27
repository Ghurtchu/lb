package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}
import org.http4s.Uri
import org.http4s.client.Client

import scala.concurrent.duration.DurationInt

object BackendHealthChecks {
  def run(
    backendUrls: Ref[IO, Urls],
    backendHealthCheckUrls: Ref[IO, Urls],
    client: Client[IO],
  ): IO[Unit] =
    (for {
      current <- backendHealthCheckUrls.getAndUpdate(_.next).map(_.current)
      _       <- IO(println(s"Checking health status of $current"))
      uri     <- IO.fromOption(
        Uri.fromString(current).toOption,
      ) {
        new RuntimeException("Could not construct proper URI")
      }
      status  <- client
        .expect[String](uri)
        .map(_ => ServerStatus.Alive)
        .recover(_ => ServerStatus.Dead)
        .timeout(5.seconds)
      _       <- status match {
        case ServerStatus.Alive =>
          for {
            _ <- IO(println("Server is alive"))
            // current = localhost:8080/hello
            backendToAdd = current.reverse.dropWhile(_ != '/').reverse.init
            _ <- backendUrls.update(_.add(backendToAdd))
          } yield ()
        case ServerStatus.Dead  =>
          for {
            _ <- IO(println("Server is dead"))
            backendToDrop = current.reverse.dropWhile(_ != '/').reverse.init
            _ <- IO(println(backendToDrop))
            _ <- backendUrls.update(_.drop(backendToDrop))
          } yield ()
      }
      _       <- backendHealthCheckUrls.update(_.next)
    } yield ()).flatMap(_ => IO.sleep(2.seconds)).foreverM
}

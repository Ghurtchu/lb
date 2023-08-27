package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}
import org.http4s.Uri
import org.http4s.client.Client

import scala.concurrent.duration.DurationInt

object HealthChecks {

  sealed trait ServerStatus

  object ServerStatus {
    case object Alive extends ServerStatus
    case object Dead  extends ServerStatus
  }
  def run(
    location: String,
    backends: Ref[IO, Backends],
    client: Client[IO],
  ): IO[Unit] =
    (for {
      current <- backends.get.map(_.current)
      currentHealthcheckURL = current concat location
      _      <- IO(println(s"checking health status of $currentHealthcheckURL"))
      _      <- IO(println(s"Checking health of $current"))
      uri    <- IO.fromOption(
        Uri.fromString(currentHealthcheckURL).toOption,
      ) {
        new RuntimeException("Could not construct proper URI")
      }
      status <- client
        .expect[String](uri)
        .map(_ => ServerStatus.Alive)
        .recover(_ => ServerStatus.Dead)
        .timeout(5.seconds)
      _      <- status match {
        case ServerStatus.Alive =>
          for {
            _ <- IO(println("Server is alive"))
            _ <- backends.update(_.add(current))
          } yield ()
        case ServerStatus.Dead  =>
          for {
            _ <- IO(println("Server is dead"))
            _ <- backends.update(_.drop(current))
          } yield ()
      }
    } yield ()).flatMap(_ => IO.sleep(2.seconds)).foreverM
}

import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.{Config, LoadBalancer, Urls}
import com.ghurtchu.loadbalancer.Urls._
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp.Simple {
  override def run: IO[Unit] =
    for {
      config       <- IO(ConfigSource.default.loadOrThrow[Config])
      backends     <- Ref
        .of[IO, Urls](config.backends)
        .map(Backends)
      healthChecks <- Ref
        .of[IO, Urls](config.healthChecks)
        .map(HealthChecks)
      (host, port) <- IO.fromOption {
        maybeHostAndPort(
          config.hostOrDefault,
          config.portOrDefault,
        )
      }(Config.InvalidConfig)
      _            <- IO.println(s"Starting server on URL: $host:$port")
      _            <- LoadBalancer.run(
        backends,
        healthChecks,
        port,
        host,
        config,
      )
    } yield ()

  private def maybeHostAndPort(
    host: String,
    port: Int,
  ): Option[(Host, Port)] =
    (
      Host.fromString(host),
      Port.fromInt(port),
    ).mapN((_, _))
}

import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.{Config, LoadBalancerServer, Urls}
import com.ghurtchu.loadbalancer.Urls._
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      cfg             <- IO.delay {
        ConfigSource.default
          .loadOrThrow[Config]
      }
      backendsRef     <- Ref.of[IO, Urls](cfg.backends)
      healthChecksRef <- Ref.of[IO, Urls](cfg.healthChecks)
      (host, port)    <- IO.fromOption {
        maybeHostAndPort(cfg.host, cfg.port)
      }(new RuntimeException("Invalid port or host from configuration"))
      _               <- IO.println(s"Starting server on URL: $host:$port")
      _               <- LoadBalancerServer.run(
        Backends(backendsRef),
        HealthChecks(healthChecksRef),
        port,
        host,
      )
    } yield ())
      .as(ExitCode.Success)

  private def maybeHostAndPort(
    hostStr: String,
    portInt: Int,
  ): Option[(Host, Port)] =
    (
      Host.fromString(hostStr),
      Port.fromInt(portInt),
    ).mapN((_, _))
}

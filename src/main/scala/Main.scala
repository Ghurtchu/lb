import cats.effect.{IO, IOApp, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.WrappedRef.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.{Config, LoadBalancer, Urls}
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      config       <- IO(ConfigSource.default.loadOrThrow[Config])
      backends     <- ref(config.backends, Backends)
      healthChecks <- ref(config.healthChecks, HealthChecks)
      (host, port) <- IO.fromEither {
        hostAndPort(
          config.hostOr(fallback = "0.0.0.0"),
          config.portOr(fallback = 8080),
        )
      }
      _            <- IO.println(s"Starting server on URL: $host:$port")
      _            <- LoadBalancer.run(
        backends,
        healthChecks,
        port,
        host,
        config.backendFromHealthCheck,
      )
    } yield ()

  private def ref[A](
    urls: Urls,
    f: Ref[IO, Urls] => A,
  ): IO[A] =
    Ref
      .of[IO, Urls](urls)
      .map(f)

  private type HostAndPortOr[A] = Either[A, (Host, Port)]

  private def hostAndPort(
    host: String,
    port: Int,
  ): HostAndPortOr[Config.InvalidConfig] =
    (
      Host.fromString(host),
      Port.fromInt(port),
    ).mapN((_, _))
      .toRight(Config.InvalidConfig)
}

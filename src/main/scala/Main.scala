import cats.effect.{IO, IOApp, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.UrlsRef.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.{Config, HttpServer, ParseUri, RoundRobin, UpdateRefUrlsAndGet, Urls}
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      config                   <- IO(ConfigSource.default.loadOrThrow[Config])
      (backends, healthChecks) <- refs(config.backends)(Backends, HealthChecks)
      (host, port)             <- IO.fromEither {
        hostAndPort(
          config.hostOr(fallback = "0.0.0.0"),
          config.portOr(fallback = 8080),
        )
      }
      _                        <- IO.println(s"Starting server on URL: $host:$port")
      _                        <- HttpServer.start(
        backends,
        healthChecks,
        port,
        host,
        ParseUri.of,
        UpdateRefUrlsAndGet.of,
        RoundRobin.of,
      )
    } yield ()

  private def refs(
    urls: Urls,
  )(
    toBackends: Ref[IO, Urls] => Backends,
    toHealthChecks: Ref[IO, Urls] => HealthChecks,
  ): IO[(Backends, HealthChecks)] =
    (
      IO.ref(urls),
      IO.ref(urls),
    ).mapN { case (first, second) =>
      (toBackends(first), toHealthChecks(second))
    }

  private def hostAndPort(
    host: String,
    port: Int,
  ): Either[Config.InvalidConfig, (Host, Port)] =
    (
      Host.fromString(host),
      Port.fromInt(port),
    ).mapN((_, _))
      .toRight(Config.InvalidConfig)
}

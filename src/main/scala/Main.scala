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
      config       <- IO(ConfigSource.default.loadOrThrow[Config])
      backends     <- ref(config.backends)(Backends)
      healthChecks <- ref(config.backends)(HealthChecks)
      (host, port) <- IO.fromEither {
        hostAndPort(
          config.hostOr(fallback = "0.0.0.0"),
          config.portOr(fallback = 8080),
        )
      }
      _            <- IO.println(s"Starting server on URL: $host:$port")
      _            <- HttpServer.start(
        backends,
        healthChecks,
        port,
        host,
        ParseUri.of,
        UpdateRefUrlsAndGet.of,
        RoundRobin.of,
      )
    } yield ()

  private def ref[A](urls: Urls)(f: Ref[IO, Urls] => A): IO[A] =
    IO.ref(urls)
      .map(f)

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

import cats.effect.{IO, IOApp, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.domain.{Config, Urls}
import com.ghurtchu.loadbalancer.domain.UrlsRef.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.http.HttpServer
import com.ghurtchu.loadbalancer.services.{ParseUri, RoundRobin, UpdateRefUrlsAndGet}
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      config                   <- IO.delay(ConfigSource.default.loadOrThrow[Config])
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
        config.healthCheckInterval,
        ParseUri.of,
        UpdateRefUrlsAndGet.of,
        RoundRobin.forBackends,
        RoundRobin.forHealthChecks,
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
    ).mapN { case (backendRef, healthCheckRef) =>
      (toBackends(backendRef), toHealthChecks(healthCheckRef))
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

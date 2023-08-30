import cats.effect.{IO, IOApp}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.UrlsRef.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.{Config, Server, ParseUri, RoundRobin, UpdateRefUrlsAndGet}
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      config       <- IO(ConfigSource.default.loadOrThrow[Config])
      backends     <- IO.ref(config.backends).map(Backends)
      healthChecks <- IO.ref(config.backends).map(HealthChecks)
      (host, port) <- IO.fromEither {
        hostAndPort(
          config.hostOr(fallback = "0.0.0.0"),
          config.portOr(fallback = 8080),
        )
      }
      _            <- IO.println(s"Starting server on URL: $host:$port")
      _            <- Server.start(
        backends,
        healthChecks,
        port,
        host,
        ParseUri.impl,
        UpdateRefUrlsAndGet.impl,
        RoundRobin.impl,
      )
    } yield ()

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

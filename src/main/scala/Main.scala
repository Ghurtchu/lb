import cats.effect.{IO, IOApp, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.Urls.RefWrapper.{Backends, HealthChecks}
import com.ghurtchu.loadbalancer.{Config, LoadBalancer, Urls}
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      config       <- IO(ConfigSource.default.loadOrThrow[Config])
      backends     <- wrappedRef(config.backends, Backends)
      healthChecks <- wrappedRef(config.healthChecks, HealthChecks)
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
        config.backendFromHealthCheck,
      )
    } yield ()

  private def wrappedRef[A](
    urls: Urls,
    f: Ref[IO, Urls] => A,
  ): IO[A] =
    Ref
      .of[IO, Urls](urls)
      .map(f)

  private def maybeHostAndPort(
    host: String,
    port: Int,
  ): Option[(Host, Port)] =
    (
      Host.fromString(host),
      Port.fromInt(port),
    ).mapN((_, _))
}

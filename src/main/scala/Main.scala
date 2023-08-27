import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.comcast.ip4s.{Host, Port}
import com.ghurtchu.loadbalancer.{Backends, Config, LoadbalancerServer}
import pureconfig._
import pureconfig.generic.auto._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      cfg          <- IO.delay(ConfigSource.default.loadOrThrow[Config])
      backends     <- Ref.of[IO, Backends](cfg.backends)
      (host, port) <- IO.fromOption(
                        maybeHostAndPort(cfg.hostStr, cfg.portInt),
                      ) {
                        new RuntimeException("Incorrect port or host")
                      }
      _            <- IO.delay(
                        println(s"Starting server on URL: $host:$port"),
                      ) *> LoadbalancerServer.run(backends, port, host)
    } yield ()).as(ExitCode.Success)

  private def maybeHostAndPort(
    hostStr: String,
    portInt: Int,
  ): Option[(Host, Port)] =
    (
      Host.fromString(hostStr),
      Port.fromInt(portInt),
    ).mapN((_, _))
}

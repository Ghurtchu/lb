import cats.effect.{ExitCode, IO, IOApp, Ref}
import com.comcast.ip4s.Port
import com.ghurtchu.loadbalancer.{Backends, LoadbalancerServer}
import pureconfig._
import pureconfig.generic.auto._

import scala.util.Try

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      config   <- IO.delay(ConfigSource.default.loadOrThrow[Config])
      backends <- Ref.of[IO, Backends](config.backends)
      port     <- IO.fromOption(maybePort(config.port))(new RuntimeException("invalid port"))
      _        <- LoadbalancerServer.run(backends, port)
    } yield ()).as(ExitCode.Success)

  private def maybePort(portStr: String): Option[Port] =
    for {
      portInt   <- Try(portStr.toInt).toOption
      maybePort <- Port.fromInt(portInt)
    } yield maybePort

  private final case class Config(port: String, backends: Backends)
}

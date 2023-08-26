import cats.effect.{ExitCode, IO, IOApp, Ref}
import com.comcast.ip4s.Port
import com.ghurtchu.loadbalancer.{Backends, LoadbalancerServer}
import pureconfig._
import pureconfig.generic.auto._

import scala.util.Try

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      cfg <- IO.fromOption(ConfigSource.default.load[Config].toOption) {
        new RuntimeException("Could not load config")
      }
      backends <- Ref.of[IO, Backends](cfg.backends)
      port <- IO.fromOption(maybePort(args.headOption.getOrElse("8080"))) {
        new RuntimeException("Could not construct port")
      }
      _ <- LoadbalancerServer.run(backends, port)
    } yield ()).as(ExitCode.Success)

  private def maybePort(portStr: String): Option[Port] = {
    for {
      portInt <- Try(portStr.toInt).toOption
      port <- Port fromInt portInt
    } yield port
  }

  final case class Config(port: String, backends: Backends)
}

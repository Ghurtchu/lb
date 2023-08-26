import cats.effect.{ExitCode, IO, IOApp, Ref}
import com.comcast.ip4s.Port
import com.ghurtchu.loadbalancer.{Backends, LoadbalancerServer}
import pureconfig._
import pureconfig.generic.auto._

import scala.util.Try

object Main extends IOApp {

  private final case class Config(port: String, backends: Backends)

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      config   <- IO.delay(ConfigSource.default.loadOrThrow[Config])
      backends <- Ref.of[IO, Backends](config.backends)
      portInt  =  Try(config.port.toInt).toOption.getOrElse(8080)
      port     <- IO.fromOption(Port.fromInt(portInt))(new RuntimeException("invalid port"))
      _        <- LoadbalancerServer.run(backends, port)
    } yield ()).as(ExitCode.Success)

}

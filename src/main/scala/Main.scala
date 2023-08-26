import cats.effect.{ExitCode, IO, IOApp, Ref}
import com.comcast.ip4s.Port
import com.ghurtchu.loadbalancer.{Backends, LoadbalancerServer}

import scala.util.Try
object Main extends IOApp {

  private val BackendsRef: IO[Ref[IO, Backends]] =
    Ref.of(Backends("http://localhost:8081/hello", "http://localhost:8082/hello"))

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      backends <- BackendsRef
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
}

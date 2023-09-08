package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.http.{HttpClient, HttpServer, ServerHealthStatus}
import cats.effect.IO
import org.http4s.{Request, Uri}
import munit.{CatsEffectSuite, FunSuite}

class SendAndExpectTest extends CatsEffectSuite:

  val localhost8080 = "localhost:8080"
  val backend       = Uri.fromString(localhost8080).toOption.get
  val emptyRequest  = Request[IO]()

  test("toBackend [Success]"):
    val sendAndExpect = SendAndExpect.toBackend(HttpClient.Hello, emptyRequest)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, "Hello")

  test("toBackend [Failure]"):
    val sendAndExpect = SendAndExpect.toBackend(HttpClient.RuntimeException, emptyRequest)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, s"server with uri: $localhost8080 is dead")

  test("toHealthCheck [Alive]"):
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClient.Hello)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Alive)

  test("toHealthCheck [Dead due to timeout]"):
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClient.TestTimeoutFailure)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Dead)

  test("toHealthCheck [Dead due to exception]"):
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClient.RuntimeException)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Dead)

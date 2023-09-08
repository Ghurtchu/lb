package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.ghurtchu.loadbalancer.http.{HttpClient, HttpServer, ServerStatus}
import munit.FunSuite
import org.http4s.{Request, Uri}

class SendAndExpectTest extends FunSuite {

  test("toBackend [Success]") {
    val emptyRequest  = Request[IO]()
    val sendAndExpect = SendAndExpect.toBackend(HttpClient.testSuccess, emptyRequest)
    val backend       = Uri.fromString("localhost:8080").toOption.get

    sendAndExpect(backend)
      .map { obtained =>
        assertEquals(obtained, "Hello")
      }
      .unsafeRunSync()
  }

  test("toBackend [Failure]") {
    val emptyRequest  = Request[IO]()
    val sendAndExpect = SendAndExpect.toBackend(HttpClient.testFailure, emptyRequest)
    val uri           = "localhost:8080"
    val backend       = Uri.fromString(uri).toOption.get

    sendAndExpect(backend)
      .map { obtained =>
        assertEquals(obtained, s"server with uri: [$uri] is dead")
      }
      .unsafeRunSync()
  }

  test("toHealthCheck [Alive]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClient.testSuccess)
    val backend       = Uri.fromString("localhost:8080").toOption.get

    sendAndExpect(backend)
      .map { obtained =>
        assertEquals(obtained, ServerStatus.Alive)
      }
      .unsafeRunSync()
  }

  test("toHealthCheck [Dead due to timeout]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClient.testTimeout)
    val backend       = Uri.fromString("localhost:8080").toOption.get

    sendAndExpect(backend)
      .map { obtained =>
        assertEquals(obtained, ServerStatus.Dead)
      }
      .unsafeRunSync()
  }

  test("toHealthCheck [Dead due to exception]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClient.testFailure)
    val backend       = Uri.fromString("localhost:8080").toOption.get

    sendAndExpect(backend)
      .map { obtained =>
        assertEquals(obtained, ServerStatus.Dead)
      }
      .unsafeRunSync()
  }
}

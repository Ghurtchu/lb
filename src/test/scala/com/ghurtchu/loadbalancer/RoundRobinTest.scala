package com.ghurtchu.loadbalancer

import UrlsRef._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite

class RoundRobinTest extends FunSuite {

  test("forBackends [Some, one url]") {
    val roundRobin = RoundRobin.forBackends
    val urls       = Urls(Vector("localhost:8082"))

    (for {
      ref <- IO.ref(urls)
      backends = Backends(ref)
      result1 <- roundRobin(backends)
      result2 <- roundRobin(backends)
    } yield assert {
      result1.exists(_.value == "localhost:8082") && result2.exists(_.value == "localhost:8082")
    })
      .unsafeRunSync()
  }

  test("forBackends [Some, multiple urls]") {
    val roundRobin = RoundRobin.forBackends
    val urls       = Urls(Vector("localhost:8081", "localhost:8082"))

    (for {
      ref <- IO.ref(urls)
      backends = Backends(ref)
      result1 <- roundRobin(backends)
      result2 <- roundRobin(backends)
      _       <- IO.println(result1)
    } yield assert {
      result1.exists(_.value == "localhost:8081") && result2.exists(_.value == "localhost:8082")
    })
      .unsafeRunSync()
  }

  test("forBackends [None]") {
    val roundRobin = RoundRobin.forBackends
    val urls       = Urls.empty

    (for {
      ref    <- IO.ref(urls)
      result <- roundRobin(Backends(ref))
    } yield assert(result.isEmpty))
      .unsafeRunSync()
  }

  test("forHealthChecks [Some, one url]") {
    val roundRobin = RoundRobin.forHealthChecks
    val urls       = Urls(Vector("localhost:8082"))

    (for {
      ref    <- IO.ref(urls)
      result <- roundRobin(HealthChecks(ref))
    } yield assert(result.value == "localhost:8082"))
      .unsafeRunSync()
  }

  test("forHealthChecks [Some, multiple urls]") {
    val roundRobin = RoundRobin.forHealthChecks
    val urls       = Urls(Vector("localhost:8081", "localhost:8082"))

    (for {
      ref     <- IO.ref(urls)
      result1 <- roundRobin(HealthChecks(ref))
      result2 <- roundRobin(HealthChecks(ref))
    } yield assert {
      result1.value == "localhost:8081" && result2.value == "localhost:8082"
    })
      .unsafeRunSync()
  }

  test("forHealthChecks [Exception, empty urls]") {
    val roundRobin = RoundRobin.forHealthChecks
    val urls       = Urls(Vector.empty)

    (for {
      ref    <- IO.ref(urls)
      result <- roundRobin(HealthChecks(ref))
        .as(false)
        .handleError {
          // thrown by Urls(...).currentUnsafe
          case _: NoSuchElementException => true
          case _                         => false
        }
    } yield assert(result))
      .unsafeRunSync()
  }
}

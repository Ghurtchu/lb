package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.domain.{Url, Urls}
import com.ghurtchu.loadbalancer.domain.UrlsRef.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite

class RoundRobinTest extends FunSuite {

  test("forBackends [Some, one url]") {
    val roundRobin = RoundRobin.forBackends
    val urls       = Urls(Vector(Url("localhost:8082")))

    (for {
      ref <- IO.ref(urls)
      backends = Backends(ref)
      assertion1 <- roundRobin(backends)
        .map(_.exists(_.value == "localhost:8082"))
      assertion2 <- roundRobin(backends)
        .map(_.exists(_.value == "localhost:8082"))
    } yield assert(assertion1 && assertion2))
      .unsafeRunSync()
  }

  test("forBackends [Some, multiple urls]") {
    val roundRobin = RoundRobin.forBackends
    val urls       = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))

    (for {
      ref <- IO.ref(urls)
      backends = Backends(ref)
      assertion1 <- roundRobin(backends)
        .map(_.exists(_.value == "localhost:8081"))
      assertion2 <- ref.get.map(_.values.map(_.value) == Vector("localhost:8082", "localhost:8081"))
      assertion3 <- roundRobin(backends)
        .map(_.exists(_.value == "localhost:8082"))
      assertion4 <- ref.get.map(_.values.map(_.value) == Vector("localhost:8081", "localhost:8082"))
    } yield assert(List(assertion1, assertion2, assertion3, assertion4).reduce(_ && _)))
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
    val urls       = Urls(Vector(Url("localhost:8082")))

    (for {
      ref    <- IO.ref(urls)
      result <- roundRobin(HealthChecks(ref))
    } yield assert(result.value == "localhost:8082"))
      .unsafeRunSync()
  }

  test("forHealthChecks [Some, multiple urls]") {
    val roundRobin = RoundRobin.forHealthChecks
    val urls       = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))

    (for {
      ref <- IO.ref(urls)
      healthChecks = HealthChecks(ref)
      assertion1 <- roundRobin(healthChecks)
        .map(_.value == "localhost:8081")
      assertion2 <- ref.get
        .map(_.values.map(_.value) == Vector("localhost:8082", "localhost:8081"))
      assertion3 <- roundRobin(healthChecks)
        .map(_.value == "localhost:8082")
      assertion4 <- ref.get
        .map(_.values.map(_.value) == Vector("localhost:8081", "localhost:8082"))
    } yield assert(List(assertion1, assertion2, assertion3, assertion4).reduce(_ && _)))
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

  test("forBackends [Some, with stateful Ref updates]") {
    val roundRobin = RoundRobin.forBackends
    val urls       = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))

    (for {
      ref <- IO.ref(urls)
      backends = Backends(ref)
      assertion1 <- roundRobin(backends) // 8082, 8081
        .map(_.exists(_.value == "localhost:8081"))
      _          <- ref.getAndUpdate { urls =>
        Urls(urls.values :+ Url("localhost:8083")) // 8082, 8081, 8083
      }
      assertion2 <- roundRobin(backends) // 8081, 8083, 8082
        .map(_.exists(_.value == "localhost:8082"))
      assertion3 <- ref.get.map { urls =>
        println(urls)

        urls.values.map(_.value) == Vector("localhost:8081", "localhost:8083", "localhost:8082")
      }
      assertion4 <- roundRobin(backends)
        .map(_.exists(_.value == "localhost:8081"))
      assertion5 <- roundRobin(backends)
        .map(_.exists(_.value == "localhost:8083"))
    } yield assert(List(assertion1, assertion2, assertion3, assertion4, assertion5).reduce(_ && _)))
      .unsafeRunSync()
  }
}

package com.ghurtchu.loadbalancer

import Urls._
import UrlsRef._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite

class UpdateRefUrlsAndGetTest extends FunSuite {

  val updateRefUrlsAndGet = UpdateRefUrlsAndGet.of
  val localhost           = Url("localhost:8083")

  test("Alive") {
    val status = HttpServer.Status.Alive
    IO.ref(Urls(Vector("localhost:8081", "localhost:8082")))
      .flatMap { ref =>
        val backends = Backends(ref)

        for {
          updated <- updateRefUrlsAndGet(backends, localhost, status)
        } yield updated.urls.contains(localhost)
      }
      .unsafeRunSync()
  }

  test("Dead") {
    val status = HttpServer.Status.Dead
    IO.ref(Urls(Vector("localhost:8081", "localhost:8082", localhost)))
      .flatMap { ref =>
        val backends = Backends(ref)

        for {
          updated <- updateRefUrlsAndGet(backends, localhost, status)
        } yield !updated.urls.contains(localhost)
      }
      .unsafeRunSync()
  }

}

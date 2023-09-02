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
    val urls   = Urls(Vector("localhost:8081", "localhost:8082"))

    (for {
      ref     <- IO.ref(urls)
      updated <- updateRefUrlsAndGet(Backends(ref), localhost, status)
    } yield updated.values == (urls.values :+ localhost))
      .unsafeRunSync()
  }

  test("Dead") {
    val status = HttpServer.Status.Dead
    val urls   = Urls(Vector("localhost:8081", "localhost:8082", localhost))

    (for {
      ref     <- IO.ref(urls)
      updated <- updateRefUrlsAndGet(Backends(ref), localhost, status)
    } yield updated.values == Vector("localhost:8081", "localhost:8082"))
      .unsafeRunSync()
  }

}

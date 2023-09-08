package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.domain.{Url, Urls}
import com.ghurtchu.loadbalancer.domain.UrlsRef.Backends
import com.ghurtchu.loadbalancer.http.HttpServer
import com.ghurtchu.loadbalancer.http.ServerStatus

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import munit.FunSuite

class UpdateBackendsAndGetTest extends FunSuite {

  val updateBackendsAndGet = UpdateBackendsAndGet.impl
  val localhost            = "localhost:8083"

  test("Alive") {
    val status = ServerStatus.Alive
    val urls   = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))

    (for {
      ref     <- IO.ref(urls)
      updated <- updateBackendsAndGet(Backends(ref), Url(localhost), status)
    } yield updated.values == (urls.values :+ localhost))
      .unsafeRunSync()
  }

  test("Dead") {
    val status = ServerStatus.Dead
    val urls   = Urls(Vector("localhost:8081", "localhost:8082", localhost).map(Url.apply))

    (for {
      ref     <- IO.ref(urls)
      updated <- updateBackendsAndGet(Backends(ref), Url(localhost), status)
    } yield updated.values == Vector("localhost:8081", "localhost:8082"))
      .unsafeRunSync()
  }

}

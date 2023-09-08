package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.domain.Urls
import com.ghurtchu.loadbalancer.domain.Url
import com.ghurtchu.loadbalancer.domain.UrlsRef.Backends
import munit.{CatsEffectSuite, FunSuite}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.ghurtchu.loadbalancer.http.HttpClient
import org.http4s.{Request, Uri}

class LoadBalancerTest extends CatsEffectSuite:

  test("All backends are inactive because Urls is empty"):
    val obtained = (for
      backends <- IO.ref(Urls.empty)
      loadBalancer = LoadBalancer.from(
        Backends(backends),
        _ => SendAndExpect.BackendSuccessTest,
        ParseUri.Impl,
        AddRequestPathToBackendUrl.Impl,
        RoundRobin.forBackends
      )
      result <- loadBalancer.orNotFound.run(Request[IO]())
    yield result.body.compile.toVector.map(bytes => String(bytes.toArray)))
      .flatten

    assertIO(obtained, "All backends are inactive")

  test("Success case"):
    val obtained = (for
      backends <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
      loadBalancer = LoadBalancer.from(
        Backends(backends),
        _ => SendAndExpect.BackendSuccessTest,
        ParseUri.Impl,
        AddRequestPathToBackendUrl.Impl,
        RoundRobin.TestOpt
      )
        result <- loadBalancer.orNotFound.run(Request[IO](uri = Uri.unsafeFromString("localhost:8080/items/1")))
    yield result.body.compile.toVector.map(bytes => String(bytes.toArray)))
      .flatten

    assertIO(obtained, "Success")

  test("Resource not found (404) case"):
    val obtained = (for
      backends <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
      emptyRequest = Request[IO]()
      loadBalancer = LoadBalancer.from(
        Backends(backends),
        _ => SendAndExpect.toBackend(HttpClient.BackendResourceNotFound, Request[IO]()),
        ParseUri.Impl,
        AddRequestPathToBackendUrl.Impl,
        RoundRobin.forBackends
      )
        result <- loadBalancer.orNotFound.run(Request[IO](uri = Uri.unsafeFromString("localhost:8080/items/1")))
    yield result.body.compile.toVector.map(bytes => String(bytes.toArray)))
      .flatten

    assertIO(obtained, s"resource at uri: localhost:8081/items/1 was not found")




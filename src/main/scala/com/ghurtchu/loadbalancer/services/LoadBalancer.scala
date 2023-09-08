package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.*
import com.ghurtchu.loadbalancer.domain.UrlsRef.*
import com.ghurtchu.loadbalancer.services.RoundRobin.BackendsRoundRobin
import org.http4s.Uri.Path.Segment
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request}

object LoadBalancer:

  def from(
    backends: Backends,
    sendAndExpectResponse: Request[IO] => SendAndExpect[String],
    parseUri: ParseUri,
    addRequestPathToBackendUrl: AddRequestPathToBackendUrl,
    backendsRoundRobin: BackendsRoundRobin,
  ): HttpRoutes[IO] =
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case request =>
      backendsRoundRobin(backends).flatMap {
        _.fold(Ok("All backends are inactive")) { backendUrl =>
          val url = addRequestPathToBackendUrl(backendUrl.value, request)
          for
            uri      <- IO.fromEither(parseUri(url))
            _        <- IO.println(uri)
            response <- sendAndExpectResponse(request)(uri)
            result   <- Ok(response)
          yield result
        }
      }
    }

package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import org.http4s.Request

trait AddRequestPathToBackendUrl:
  def apply(backendUrl: String, request: Request[IO]): String

object AddRequestPathToBackendUrl:

  object Impl extends AddRequestPathToBackendUrl:
    override def apply(backendUrl: String, request: Request[IO]): String =
      val requestPath = request.uri.path.renderString
        .dropWhile(_ != '/')

      backendUrl concat requestPath

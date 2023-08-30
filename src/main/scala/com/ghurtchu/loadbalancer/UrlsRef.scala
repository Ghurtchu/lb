package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}

sealed trait UrlsRef {
  def urls: Ref[IO, Urls]
}

object UrlsRef {
  final case class Backends(urls: Ref[IO, Urls])     extends UrlsRef
  final case class HealthChecks(urls: Ref[IO, Urls]) extends UrlsRef
}

package com.ghurtchu.loadbalancer.domain

import cats.effect.{IO, Ref}

trait UrlsRef {
  def urls: Ref[IO, Urls]
}

object UrlsRef {
  final case class Backends(urls: Ref[IO, Urls])     extends UrlsRef
  final case class HealthChecks(urls: Ref[IO, Urls]) extends UrlsRef
}

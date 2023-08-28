package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}

final case class Urls(urls: Vector[String]) extends AnyVal {

  def next: Urls =
    copy(urls.tail :+ urls.head)

  def current: String =
    urls.head

  def remove(url: String): Urls =
    copy(urls.filter(_ != url))

  def add(url: String): Urls =
    copy(urls :+ url)
}

object Urls {

  def empty: Urls = Urls(Vector.empty)

  sealed trait WrappedRef {
    def ref: Ref[IO, Urls]
  }

  object WrappedRef {
    final case class Backends(ref: Ref[IO, Urls])     extends WrappedRef
    final case class HealthChecks(ref: Ref[IO, Urls]) extends WrappedRef
  }
}

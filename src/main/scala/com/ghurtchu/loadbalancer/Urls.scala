package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}

final case class Urls(urls: Vector[String]) extends AnyVal {

  private def set: Set[String] =
    urls.toSet

  def next: Urls =
    copy(urls.tail :+ urls.head)

  def current: String =
    urls.head

  def remove(url: String): Urls =
    copy((set - url).toVector)

  def add(url: String): Urls =
    copy((set + url).toVector)
}

object Urls {
  final case class Backends(ref: Ref[IO, Urls])     extends AnyVal
  final case class HealthChecks(ref: Ref[IO, Urls]) extends AnyVal
}

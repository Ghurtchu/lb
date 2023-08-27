package com.ghurtchu.loadbalancer

final case class Backends(urls: Vector[String]) extends AnyVal {
  private def set: Set[String] = urls.toSet

  def next: Backends = {
    val head        = urls.head
    val urlsUpdated = urls.tail :+ head

    copy(urlsUpdated)
  }

  def current: String = urls.head

  def drop(url: String): Backends = copy((set - url).toVector)

  def add(url: String): Backends = copy((set + url).toVector)
}

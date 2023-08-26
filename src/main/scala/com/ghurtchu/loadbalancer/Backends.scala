package com.ghurtchu.loadbalancer

final case class Backends(urls: Vector[String]) extends AnyVal {

  def next: Backends = {
    val head = urls.head
    val urlsUpdated = urls.tail :+ head

    copy(urlsUpdated)
  }

  def current: String = urls.head

}

object Backends {
  def empty: Backends = Backends(Vector.empty)
}
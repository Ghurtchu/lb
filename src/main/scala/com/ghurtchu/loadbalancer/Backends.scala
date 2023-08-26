package com.ghurtchu.loadbalancer

final case class Backends private(urls: Vector[String]) extends AnyVal {

  def next: Backends = {
    val head = urls.head
    val urlsUpdated = urls.tail :+ head

    copy(urlsUpdated)
  }

  def current: String = urls.head

}

object Backends {

  import Option._

  def apply(urls: String*): Backends =
    when(urls.length >= 2)(new Backends(urls.toVector))
      .getOrElse(throw new RuntimeException("at least two urls must be provided"))
}

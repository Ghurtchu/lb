package com.ghurtchu.loadbalancer

final case class Urls(urls: Vector[String]) extends AnyVal {

  def next: Urls =
    copy(urls.tail :+ urls.head)

  def current: String =
    urls.head

  def remove(url: String): Urls =
    copy(urls.filter(_ != url))

  def add(url: String): Urls =
    copy {
      if (urls contains url)
        urls
      else
        urls :+ url
    }
}

object Urls {

  def empty: Urls = Urls(Vector.empty)
}

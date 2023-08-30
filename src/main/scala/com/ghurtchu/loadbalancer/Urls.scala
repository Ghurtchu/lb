package com.ghurtchu.loadbalancer

import com.ghurtchu.loadbalancer.Urls.Url

final case class Urls(urls: Vector[Url]) extends AnyVal {

  def next: Urls = copy(urls.tail :+ urls.head)

  def current: Url = urls.head

  def remove(url: Url): Urls = copy(urls.filter(_ != url))

  def add(url: Url): Urls = if (urls contains url) this else copy(urls :+ url)
}

object Urls {

  final case class Url(value: String) extends AnyVal

  implicit def stringToBackendUrl: String => Url = Url

  def empty: Urls = Urls(Vector.empty)
}

package com.ghurtchu.loadbalancer

import com.ghurtchu.loadbalancer.Urls.BackendUrl

final case class Urls(urls: Vector[BackendUrl]) extends AnyVal {

  def next: Urls = copy(urls.tail :+ urls.head)

  def current: BackendUrl = urls.head

  def remove(url: BackendUrl): Urls = copy(urls.filter(_ != url))

  def add(url: BackendUrl): Urls = if (urls contains url) this else copy(urls :+ url)
}

object Urls {

  final case class BackendUrl(value: String) extends AnyVal

  implicit def stringToBackendUrl: String => BackendUrl = BackendUrl

  def empty: Urls = Urls(Vector.empty)
}

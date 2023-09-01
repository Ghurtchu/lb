package com.ghurtchu.loadbalancer

import com.ghurtchu.loadbalancer.Urls.Url

import scala.util.Try

final case class Urls(urls: Vector[Url]) extends AnyVal {

  def next: Urls =
    if (urls.isEmpty) copy(Vector.empty)
    else if (urls.length == 1) copy(Vector(urls.head))
    else copy(urls.tail :+ urls.head)
  def currentOpt: Option[Url] = Try(currentUnsafe).toOption

  def currentUnsafe: Url = urls.head

  def remove(url: Url): Urls = copy(urls.filter(_ != url))

  def add(url: Url): Urls = if (urls contains url) this else copy(urls :+ url)
}

object Urls {

  final case class Url(value: String) extends AnyVal {
    override def toString: String = value
  }

  implicit def stringToBackendUrl: String => Url = Url

  def empty: Urls = Urls(Vector.empty)
}

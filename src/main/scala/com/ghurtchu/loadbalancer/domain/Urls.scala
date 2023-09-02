package com.ghurtchu.loadbalancer.domain

import com.ghurtchu.loadbalancer.domain.Urls.Url

import scala.util.Try

final case class Urls(values: Vector[Url]) extends AnyVal {

  def next: Urls = Try(copy(values.tail :+ values.head)).getOrElse(Urls.empty)

  def currentOpt: Option[Url] = Try(currentUnsafe).toOption

  def currentUnsafe: Url = values.head

  def remove(url: Url): Urls = copy(values.filter(_ != url))

  def add(url: Url): Urls = if (values contains url) this else copy(values :+ url)
}

object Urls {

  final case class Url(value: String) extends AnyVal {
    override def toString: String = value
  }

  implicit def stringToBackendUrl: String => Url = Url

  def empty: Urls = Urls(Vector.empty)
}

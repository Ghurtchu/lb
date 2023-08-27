package com.ghurtchu.loadbalancer

final case class Urls(urls: Vector[String]) extends AnyVal {
  private def set: Set[String] = urls.toSet

  def next: Urls = {
    val head        = urls.head
    val urlsUpdated = urls.tail :+ head

    copy(urlsUpdated)
  }

  def current: String = urls.head

  def drop(url: String): Urls = {
    val urlsUpdated = (set - url).toVector

    copy(urlsUpdated)
  }

  def add(url: String): Urls = {
    val urlsUpdated = (set + url).toVector

    copy(urlsUpdated)
  }
}

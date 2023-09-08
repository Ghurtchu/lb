package com.ghurtchu.loadbalancer.domain

import com.ghurtchu.loadbalancer.domain.Url

import scala.util.Try

final case class Urls(values: Vector[Url]):

  def next: Urls =
    Try(copy(values.tail :+ values.head))
      .getOrElse(Urls.empty)

  def currentOpt: Option[Url] =
    Try(currentUnsafe).toOption

  def currentUnsafe: Url =
    values.head

  def remove(url: Url): Urls =
    copy(values.filter(_ != url))

  def add(url: Url): Urls =
    if (values contains url) this
    else copy(values :+ url)

object Urls:

  def empty: Urls = Urls(Vector.empty)

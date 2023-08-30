package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.Url

trait RoundRobin {
  def apply(ref: UrlsRef): IO[Url]
}

object RoundRobin {

  def of: RoundRobin = new RoundRobin {
    override def apply(ref: UrlsRef): IO[Url] =
      ref.urls
        .getAndUpdate(_.next)
        .map(_.current)
  }
}

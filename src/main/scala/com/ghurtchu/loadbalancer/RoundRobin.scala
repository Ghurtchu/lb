package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.BackendUrl

trait RoundRobin {
  def apply(ref: UrlsRef): IO[BackendUrl]
}

object RoundRobin {

  def of: RoundRobin = new RoundRobin {
    override def apply(ref: UrlsRef): IO[BackendUrl] =
      ref.urls
        .getAndUpdate(_.next)
        .map(_.current)
  }
}

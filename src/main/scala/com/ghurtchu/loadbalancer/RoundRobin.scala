package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.BackendUrl

trait RoundRobin {
  def apply(urlsRef: UrlsRef): IO[BackendUrl]
}

object RoundRobin {

  def of: RoundRobin = _.urls
    .getAndUpdate(_.next)
    .map(_.current)
}

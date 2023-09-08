package com.ghurtchu.loadbalancer.services

import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.Urls.Url
import com.ghurtchu.loadbalancer.domain.UrlsRef.Backends
import com.ghurtchu.loadbalancer.domain.Urls
import com.ghurtchu.loadbalancer.http.HttpServer.Status

trait UpdateBackendsAndGet {
  def apply(backends: Backends, url: Url, status: Status): IO[Urls]
}

object UpdateBackendsAndGet {

  def impl: UpdateBackendsAndGet = new UpdateBackendsAndGet {
    override def apply(backends: Backends, url: Url, status: Status): IO[Urls] =
      status match {
        case Status.Alive =>
          IO.println(s"$url is alive") *>
            backends.urls
              .updateAndGet(_.add(url))
        case Status.Dead  =>
          IO.println(s"$url is dead") *>
            backends.urls
              .updateAndGet(_.remove(url))
      }
  }
}

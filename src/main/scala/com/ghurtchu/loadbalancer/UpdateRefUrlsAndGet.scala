package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.HttpServer.Status
import com.ghurtchu.loadbalancer.Urls.BackendUrl

trait UpdateRefUrlsAndGet {
  def apply(ref: UrlsRef, url: BackendUrl, status: Status): IO[Urls]
}

object UpdateRefUrlsAndGet {

  def of: UpdateRefUrlsAndGet = (ref, url, status) =>
    status match {
      case Status.Alive =>
        IO.println(s"$url is alive") *>
          ref.urls
            .updateAndGet(_.add(url))
      case Status.Dead  =>
        IO.println(s"$url is dead") *>
          ref.urls
            .updateAndGet(_.remove(url))
    }
}

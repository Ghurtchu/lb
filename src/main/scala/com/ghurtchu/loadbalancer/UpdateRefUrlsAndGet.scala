package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Server.ServerStatus
import com.ghurtchu.loadbalancer.Urls.BackendUrl

trait UpdateRefUrlsAndGet {
  def apply(ref: UrlsRef, url: BackendUrl, ss: ServerStatus): IO[Urls]
}

object UpdateRefUrlsAndGet {

  def impl: UpdateRefUrlsAndGet = (ref, url, ss) =>
    ss match {
      case ServerStatus.Alive =>
        IO.println(s"$url is alive") *>
          ref.urls
            .updateAndGet(_.add(url))
      case ServerStatus.Dead  =>
        IO.println(s"$url is dead") *>
          ref.urls
            .updateAndGet(_.remove(url))
    }
}

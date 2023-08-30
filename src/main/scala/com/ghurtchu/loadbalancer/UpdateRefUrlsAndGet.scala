package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.HttpServer.Status
import com.ghurtchu.loadbalancer.Urls.Url

trait UpdateRefUrlsAndGet {
  def apply(ref: UrlsRef, url: Url, status: Status): IO[Urls]
}

object UpdateRefUrlsAndGet {

  def of: UpdateRefUrlsAndGet = new UpdateRefUrlsAndGet {
    override def apply(ref: UrlsRef, url: Url, status: Status): IO[Urls] =
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
}

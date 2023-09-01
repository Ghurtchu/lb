package com.ghurtchu.loadbalancer

import cats.Id
import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.Url

trait RoundRobin[F[_]] {
  def apply(ref: UrlsRef): IO[F[Url]]
}

object RoundRobin {

  def forBackends: RoundRobin[Option] = new RoundRobin[Option] {
    override def apply(ref: UrlsRef): IO[Option[Url]] =
      ref.urls
        .getAndUpdate(_.next)
        .map(_.currentOpt)

  }
  def forHealthChecks: RoundRobin[Id] = new RoundRobin[Id] {
    override def apply(ref: UrlsRef): IO[Id[Url]] =
      ref.urls
        .getAndUpdate(_.next)
        .map(_.currentUnsafe)
  }
}

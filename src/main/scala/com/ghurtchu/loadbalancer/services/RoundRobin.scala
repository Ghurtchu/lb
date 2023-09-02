package com.ghurtchu.loadbalancer.services

import cats.Id
import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.Urls.Url
import com.ghurtchu.loadbalancer.domain.UrlsRef

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

  val testId: RoundRobin[Id] = new RoundRobin[Id] {
    override def apply(ref: UrlsRef): IO[Id[Url]] =
      IO.pure(Url("localhost:8081"))
  }

  val testOpt: RoundRobin[Option] = new RoundRobin[Option] {
    override def apply(ref: UrlsRef): IO[Option[Url]] =
      IO.pure(Some(Url("localhost:8081")))
  }
}

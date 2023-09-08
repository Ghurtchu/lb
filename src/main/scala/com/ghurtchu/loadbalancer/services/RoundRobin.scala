package com.ghurtchu.loadbalancer.services

import cats.Id
import cats.effect.IO
import com.ghurtchu.loadbalancer.domain.Url
import com.ghurtchu.loadbalancer.domain.UrlsRef
import cats.syntax.option._

trait RoundRobin[F[_]]:
  def apply(ref: UrlsRef): IO[F[Url]]

object RoundRobin:

  type BackendsRoundRobin     = RoundRobin[Option]
  type HealthChecksRoundRobin = RoundRobin[Id]

  def forBackends: BackendsRoundRobin = new BackendsRoundRobin:
    override def apply(ref: UrlsRef): IO[Option[Url]] =
      ref.urls
        .getAndUpdate(_.next)
        .map(_.currentOpt)

  def forHealthChecks: HealthChecksRoundRobin = new HealthChecksRoundRobin:
    override def apply(ref: UrlsRef): IO[Id[Url]] =
      ref.urls
        .getAndUpdate(_.next)
        .map(_.currentUnsafe)

  val TestId: RoundRobin[Id] = _ => IO.pure(Url("localhost:8081"))

  val TestOpt: RoundRobin[Option] = _ => IO.pure(Some(Url("localhost:8081")))

package com.ghurtchu.loadbalancer

import cats.effect.{IO, Ref}

sealed trait WrappedRef {
  def ref: Ref[IO, Urls]
}

object WrappedRef {
  final case class Backends(ref: Ref[IO, Urls])     extends WrappedRef
  final case class HealthChecks(ref: Ref[IO, Urls]) extends WrappedRef
}

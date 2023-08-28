package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.WrappedRef

trait RoundRobin {
  def apply(wrappedRef: WrappedRef): IO[String]
}

object RoundRobin {

  def live: RoundRobin =
    _.ref
      .getAndUpdate(_.next)
      .map(_.current)
}

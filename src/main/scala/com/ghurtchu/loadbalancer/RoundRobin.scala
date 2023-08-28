package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.RefWrapper

trait RoundRobin {
  def apply(refWrapper: RefWrapper): IO[String]
}

object RoundRobin {

  def of: RoundRobin = _.ref
    .getAndUpdate(_.next)
    .map(_.current)
}

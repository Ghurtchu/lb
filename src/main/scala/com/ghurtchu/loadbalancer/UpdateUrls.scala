package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.RefWrapper

trait UpdateUrls {
  def apply(
    refWrapper: RefWrapper,
    status: ServerStatus,
    backendUrl: String,
  ): IO[Urls]
}

object UpdateUrls {

  def live: UpdateUrls = (refWrapper, status, backendUrl) =>
    status match {
      case ServerStatus.Alive =>
        IO.println(s"$backendUrl is alive") *>
          refWrapper.ref
            .updateAndGet(_.add(backendUrl))
      case ServerStatus.Dead  =>
        IO.println(s"$backendUrl is dead") *>
          refWrapper.ref
            .updateAndGet(_.remove(backendUrl))
    }
}

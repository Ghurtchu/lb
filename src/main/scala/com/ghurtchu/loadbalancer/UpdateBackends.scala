package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.RefWrapper.Backends

trait UpdateBackends {
  def apply(status: ServerStatus, backendUrl: String): IO[Urls]
}

object UpdateBackends {
  def of(backends: Backends): UpdateBackends = (status, backendUrl) =>
    status match {
      case ServerStatus.Alive =>
        IO.println(s"$backendUrl is alive") *>
          backends.ref
            .updateAndGet(_.add(backendUrl))
      case ServerStatus.Dead  =>
        IO.println(s"$backendUrl is dead") *>
          backends.ref
            .updateAndGet(_.remove(backendUrl))
    }
}

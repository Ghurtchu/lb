package com.ghurtchu.loadbalancer

import cats.effect.IO
import com.ghurtchu.loadbalancer.Urls.WrappedRef

trait UpdateWrappedRefUrls {
  def apply(
    wrappedRef: WrappedRef,
    serverStatus: ServerStatus,
    backendUrl: String,
  ): IO[Urls]
}

object UpdateWrappedRefUrls {

  def live: UpdateWrappedRefUrls = (wrappedRef, serverStatus, backendUrl) =>
    serverStatus match {
      case ServerStatus.Alive =>
        IO.println(s"$backendUrl is alive") *>
          wrappedRef.ref
            .updateAndGet(_.add(backendUrl))
      case ServerStatus.Dead  =>
        IO.println(s"$backendUrl is dead") *>
          wrappedRef.ref
            .updateAndGet(_.remove(backendUrl))
    }
}

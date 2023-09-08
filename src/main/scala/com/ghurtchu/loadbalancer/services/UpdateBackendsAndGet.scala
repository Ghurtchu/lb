package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.domain.*
import com.ghurtchu.loadbalancer.domain.UrlsRef.Backends
import com.ghurtchu.loadbalancer.domain.Url
import com.ghurtchu.loadbalancer.domain.Urls
import com.ghurtchu.loadbalancer.http.ServerHealthStatus
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import cats.effect.IO

trait UpdateBackendsAndGet:
  def apply(
    backends: Backends,
    url: Url,
    status: ServerHealthStatus,
  ): IO[Urls]

object UpdateBackendsAndGet:

  object Impl extends UpdateBackendsAndGet:
    override def apply(
      backends: Backends,
      url: Url,
      status: ServerHealthStatus,
    ): IO[Urls] =
      backends.urls.updateAndGet { urls =>
        status match
          case ServerHealthStatus.Alive => urls.add(url)
          case ServerHealthStatus.Dead  => urls.remove(url)
      }

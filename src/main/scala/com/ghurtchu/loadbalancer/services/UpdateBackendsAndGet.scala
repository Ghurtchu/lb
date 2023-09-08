package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.domain.*
import com.ghurtchu.loadbalancer.domain.UrlsRef.Backends
import com.ghurtchu.loadbalancer.domain.Url
import com.ghurtchu.loadbalancer.domain.Urls
import com.ghurtchu.loadbalancer.http.ServerStatus
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import cats.effect.IO

trait UpdateBackendsAndGet:
  def apply(backends: Backends, url: Url, status: ServerStatus): IO[Urls]

object UpdateBackendsAndGet:

  def impl: UpdateBackendsAndGet = new UpdateBackendsAndGet:
    override def apply(backends: Backends, url: Url, status: ServerStatus): IO[Urls] =
      backends.urls.updateAndGet { urls =>
        status match
          case ServerStatus.Alive => urls.add(url)
          case ServerStatus.Dead  => urls.remove(url)
      }

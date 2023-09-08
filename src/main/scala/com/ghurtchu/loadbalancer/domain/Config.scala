package com.ghurtchu.loadbalancer.domain

import com.ghurtchu.loadbalancer.domain.Url
import pureconfig.ConfigReader
import pureconfig._
import pureconfig.generic.derivation.default._

import scala.util.Try

import Config._

final case class Config(
  port: String,
  host: String,
  backends: Urls,
  healthCheckInterval: HealthCheckInterval,
) derives ConfigReader:

  def hostOr(fallback: String): String =
    if (host.isEmpty) fallback
    else host

  def portOr(fallback: Int): Int =
    Try(port.toInt).toOption
      .getOrElse(fallback)

object Config:

  given urlsReader: ConfigReader[Urls] = ConfigReader[Vector[Url]].map(Urls.apply)
  given urlReader: ConfigReader[Url]   = ConfigReader[String].map(Url.apply)

  given healthCheckReader: ConfigReader[HealthCheckInterval] =
    ConfigReader[Long].map(HealthCheckInterval.apply)

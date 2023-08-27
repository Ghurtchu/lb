package com.ghurtchu.loadbalancer

import scala.util.Try

final case class Config(
  rawPort: String,
  rawHost: String,
  backends: Urls,
  healthChecks: Urls,
) {

  def host: String =
    if (rawHost.isEmpty)
      "0.0.0.0"
    else
      rawHost

  def port: Int =
    Try(rawPort.toInt).toOption
      .getOrElse(8080)
}

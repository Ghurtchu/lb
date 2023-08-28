package com.ghurtchu.loadbalancer

import scala.util.Try

final case class Config(
  port: String,
  host: String,
  backends: Urls,
  healthChecks: Urls,
) {

  def hostOrDefault: String =
    if (host.isEmpty)
      "0.0.0.0"
    else
      host

  def portOrDefault: Int =
    Try(port.toInt).toOption
      .getOrElse(8080)

  def backendFromHealthCheck(healthCheckUrl: String): String =
    healthCheckUrl.reverse
      .dropWhile(_ != '/')
      .reverse
      .init
}

object Config {

  final case object InvalidConfig extends Throwable {
    override def getMessage: String =
      "Invalid port or host, please fix Config"
  }
}

package com.ghurtchu.loadbalancer

import scala.util.Try

final case class Config(port: String, host: String, backends: Urls) {

  def hostOr(fallback: String): String =
    if (host.isEmpty) fallback else host

  def portOr(fallback: Int): Int =
    Try(port.toInt).toOption.getOrElse(fallback)
}

object Config {

  type InvalidConfig = InvalidConfig.type

  final case object InvalidConfig extends Throwable {
    override def getMessage: String =
      "Invalid port or host, please fix Config"
  }
}

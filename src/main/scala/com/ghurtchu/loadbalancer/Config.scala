package com.ghurtchu.loadbalancer

import scala.util.Try

final case class Config(
  port: String,
  host: String,
  backends: Backends,
) {
  def hostStr: String = if (host.isEmpty) "0.0.0.0" else host
  def portInt: Int = Try(port.toInt).toOption.getOrElse(8080)
}

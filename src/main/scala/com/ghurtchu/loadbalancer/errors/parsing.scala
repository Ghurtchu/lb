package com.ghurtchu.loadbalancer.errors

object parsing:

  final case class InvalidUri(uri: String) extends Throwable:
    override def getMessage: String =
      s"Could not construct proper URI from $uri"

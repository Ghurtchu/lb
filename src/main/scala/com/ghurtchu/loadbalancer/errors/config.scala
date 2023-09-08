package com.ghurtchu.loadbalancer.errors

object config:

  type InvalidConfig = InvalidConfig.type

  case object InvalidConfig extends Throwable:
    override def getMessage: String =
      "Invalid port or host, please fix Config"
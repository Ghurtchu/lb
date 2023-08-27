package com.ghurtchu.loadbalancer

sealed trait ServerStatus

object ServerStatus {
  case object Alive extends ServerStatus
  case object Dead  extends ServerStatus
}

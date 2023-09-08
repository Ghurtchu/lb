package com.ghurtchu.loadbalancer.services

trait AddRequestPathToBackendUrl:
  def apply(backendUrl: String, requestPath: String): String

object AddRequestPathToBackendUrl:

  object Impl extends AddRequestPathToBackendUrl:
    override def apply(backendUrl: String, requestPath: String): String =
      backendUrl.concat(requestPath)

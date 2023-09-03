package com.ghurtchu.loadbalancer.services

import cats.syntax.either._
import com.ghurtchu.loadbalancer.services.LoadBalancer.InvalidUri
import org.http4s.Uri

trait ParseUri {
  def apply(uri: String): Either[InvalidUri, Uri]
}

object ParseUri {

  def impl: ParseUri = new ParseUri {
    override def apply(uri: String): Either[InvalidUri, Uri] =
      Uri
        .fromString(uri)
        .leftMap(_ => InvalidUri(uri))
  }
}

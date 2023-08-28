package com.ghurtchu.loadbalancer

import com.ghurtchu.loadbalancer.Routes.InvalidUri
import org.http4s.Uri
import cats.syntax.either._

trait ParseUri {
  def apply(uri: String): Either[InvalidUri, Uri]
}

object ParseUri {

  def live: ParseUri = uri =>
    Uri
      .fromString(uri)
      .leftMap(_ => InvalidUri(uri))
}

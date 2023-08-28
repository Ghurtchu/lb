package com.ghurtchu.loadbalancer

import com.ghurtchu.loadbalancer.Routes.InvalidURI
import org.http4s.Uri

trait ParseUri {
  def apply(uri: String): Either[InvalidURI, Uri]
}

object ParseUri {

  def live: ParseUri = (uri: String) =>
    Uri
      .fromString(uri)
      .left
      .map(_ => InvalidURI(uri))
}

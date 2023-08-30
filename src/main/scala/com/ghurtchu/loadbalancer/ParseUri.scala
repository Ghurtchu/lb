package com.ghurtchu.loadbalancer

import com.ghurtchu.loadbalancer.LoadBalancer.InvalidUri
import org.http4s.Uri
import cats.syntax.either._

trait ParseUri {
  def apply(uri: String): Either[InvalidUri, Uri]
}

object ParseUri {

  def of: ParseUri = new ParseUri {
    override def apply(uri: String): Either[InvalidUri, Uri] =
      Uri
        .fromString(uri)
        .leftMap(_ => InvalidUri(uri))
  }
}

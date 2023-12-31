package com.ghurtchu.loadbalancer.services

import cats.syntax.either._
import com.ghurtchu.loadbalancer.errors.parsing.InvalidUri
import org.http4s.Uri

trait ParseUri:
  def apply(uri: String): Either[InvalidUri, Uri]

object ParseUri:

  object Impl extends ParseUri:
    override def apply(uri: String): Either[InvalidUri, Uri] =
      Uri
        .fromString(uri)
        .leftMap(_ => InvalidUri(uri))

package com.ghurtchu.loadbalancer.services

import com.ghurtchu.loadbalancer.services.LoadBalancer.InvalidUri
import munit.FunSuite
import org.http4s.Uri

class ParseUriTest extends FunSuite {

  val parseUri = ParseUri.of

  test("valid URI") {
    val uri      = "0.0.0.0/8080"
    val obtained = parseUri(uri)
    val expected = Right(Uri.unsafeFromString(uri))

    assertEquals(obtained, expected)
  }

  test("invalid URI") {
    val uri      = "definitely invalid uri XD"
    val obtained = parseUri(uri)
    val expected = Left(InvalidUri(uri))

    assertEquals(obtained, expected)
  }
}

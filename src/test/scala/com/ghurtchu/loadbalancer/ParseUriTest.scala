package com.ghurtchu.loadbalancer

import com.ghurtchu.loadbalancer.Routes.InvalidUri
import munit.FunSuite
import org.http4s.Uri

class ParseUriTest extends FunSuite {

  val parser = ParseUri.live

  test("valid URI") {
    val uriStr   = "0.0.0.0/8080"
    val obtained = parser(uriStr)
    val expected = Right(Uri.unsafeFromString(uriStr))

    assertEquals(obtained, expected)
  }

  test("invalid URI") {
    val uriStr   = "definitely invalid uri"
    val obtained = parser(uriStr)
    val expected = Left(InvalidUri(uriStr))

    assertEquals(obtained, expected)
  }
}

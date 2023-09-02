package com.ghurtchu.loadbalancer

import munit.FunSuite
import Urls._

class UrlsTest extends FunSuite {

  private def sequentialUrls(from: Int, to: Int): Urls = Urls {
    (from to to)
      .map(i => Url(s"url$i"))
      .toVector
  }

  test("next [success]") {
    val urls     = sequentialUrls(1, 5)
    val obtained = urls.next
    val expected = Urls(sequentialUrls(2, 5).values :+ "url1")

    assertEquals(obtained, expected)
  }

  test("next [1 value]") {
    val urls     = Urls(Vector("url1"))
    val obtained = urls.next
    println(obtained)
    val expected = urls

    assertEquals(obtained, expected)
  }

  test("currentOpt [success]") {
    val urls     = sequentialUrls(1, 5)
    val obtained = urls.currentOpt.map(_.value)
    val expected = Some("url1")

    assertEquals(obtained, expected)
  }

  test("currentOpt [failure]") {
    val urls     = Urls.empty
    val obtained = urls.currentOpt.map(_.value)
    val expected = None

    assertEquals(obtained, expected)
  }

  test("currentUnsafe [success]") {
    val urls     = sequentialUrls(1, 5)
    val obtained = urls.currentUnsafe.value
    val expected = "url1"

    assertEquals(obtained, expected)
  }

  test("currentUnsafe [failure]") {
    intercept[NoSuchElementException] {
      Urls.empty.currentUnsafe
    }
  }

  test("remove") {
    val urls     = sequentialUrls(1, 5)
    val obtained = urls.remove("url1")
    val expected = sequentialUrls(2, 5)

    assertEquals(obtained, expected)
  }

  test("add") {
    val urls     = sequentialUrls(2, 5)
    val obtained = urls.add("url1")
    val expected = Urls(urls.values :+ "url1")

    assertEquals(obtained, expected)
  }
}

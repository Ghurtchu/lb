package com.ghurtchu.loadbalancer

import munit.FunSuite

class UrlsTest extends FunSuite {

  private def sequentialUrls(from: Int, to: Int): Urls = Urls {
    (from to to)
      .map(i => s"url$i")
      .toVector
  }

  test("next [success]") {
    val urls     = sequentialUrls(1, 5)
    val obtained = urls.next
    val expected = Urls(sequentialUrls(2, 5).urls :+ "url1")

    assertEquals(obtained, expected)
  }

  test("current [success]") {
    val urls     = sequentialUrls(1, 5)
    val obtained = urls.current
    val expected = "url1"

    assertEquals(obtained, expected)
  }

  test("current [failure]") {
    intercept[NoSuchElementException] {
      Urls.empty.current
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
    val expected = Urls(urls.urls :+ "url1")

    assertEquals(obtained, expected)
  }
}

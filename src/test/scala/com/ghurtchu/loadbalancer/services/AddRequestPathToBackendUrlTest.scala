package com.ghurtchu.loadbalancer.services

import munit.FunSuite

class AddRequestPathToBackendUrlTest extends FunSuite:

  val impl          = AddRequestPathToBackendUrl.Impl
  val localhost8080 = "http://localhost:8080"

  test("with request path"):
    val obtained = impl(backendUrl = localhost8080, requestPath = "/items/1")
    val expected = "http://localhost:8080/items/1"

    assertEquals(obtained, expected)

  test("without request path"):
    val obtained = impl(backendUrl = localhost8080, requestPath = "")
    val expected = localhost8080

    assertEquals(obtained, expected)

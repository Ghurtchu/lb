package com.ghurtchu.loadbalancer

import munit.FunSuite

class ConfigTest extends FunSuite {

  val config = Config(
    port = "8081",
    host = "localhost",
    backends = Urls.empty,
  )

  test("hostOr") {
    val obtained = config.hostOr("0.0.0.0")
    val expected = "localhost"
    assertEquals(obtained, expected)

    val configWithEmptyHost = config.copy(host = "")
    val obtainedDefault     = configWithEmptyHost.hostOr("0.0.0.0")
    val expectedDefault     = "0.0.0.0"
    assertEquals(obtainedDefault, expectedDefault)
  }

  test("portOr") {
    val obtained = config.portOr(8080)
    val expected = 8081
    assertEquals(obtained, expected)

    val configWithEmptyPort = config.copy(port = "invalid port")
    val obtainedDefault     = configWithEmptyPort.portOr(8080)
    val expectedDefault     = 8080
    assertEquals(obtainedDefault, expectedDefault)
  }
}

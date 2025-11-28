package org.robolectric.integrationtests.junit.vintage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** The test class for basic JUnit jupiter. */
internal class BasicJUnitJupiterTest {
  private val calculator: Calculator = Calculator()

  @Test
  fun addition() {
    assertEquals(2, calculator.add(1, 1))
  }

  private class Calculator {
    fun add(left: Int, right: Int): Int = left + right
  }
}

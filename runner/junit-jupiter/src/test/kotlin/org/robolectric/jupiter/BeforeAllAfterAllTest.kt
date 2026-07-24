package org.robolectric.jupiter

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BeforeAllAfterAllTest {
  companion object {
    var beforeAllCalled = false
    var afterAllCalled = false
    val callOrder = mutableListOf<String>()

    @JvmStatic
    @BeforeAll
    fun setupClass() {
      beforeAllCalled = true
      callOrder.add("beforeAll")
    }

    @JvmStatic
    @AfterAll
    fun teardownClass() {
      afterAllCalled = true
      callOrder.add("afterAll")
    }
  }

  @Test
  fun test1() {
    assertThat(beforeAllCalled).isTrue()
    callOrder.add("test1")
  }

  @Test
  fun test2() {
    assertThat(beforeAllCalled).isTrue()
    callOrder.add("test2")
  }

  @Test
  fun verifyBeforeAllRunsOnce() {
    assertThat(callOrder.filter { it == "beforeAll" }).hasSize(1)
  }
}

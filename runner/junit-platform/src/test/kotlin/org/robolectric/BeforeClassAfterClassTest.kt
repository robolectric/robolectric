package org.robolectric

import com.google.common.truth.Truth.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class BeforeClassAfterClassTest {

  companion object {
    var beforeClassCalled = false
    var afterClassCalled = false
    val callOrder = mutableListOf<String>()

    @JvmStatic
    @BeforeClass
    fun setupClass() {
      beforeClassCalled = true
      callOrder.add("beforeClass")
    }

    @JvmStatic
    @AfterClass
    fun teardownClass() {
      afterClassCalled = true
      callOrder.add("afterClass")
    }
  }

  @Test
  fun test1() {
    assertThat(beforeClassCalled).isTrue()
    callOrder.add("test1")
  }

  @Test
  fun test2() {
    assertThat(beforeClassCalled).isTrue()
    callOrder.add("test2")
  }
}

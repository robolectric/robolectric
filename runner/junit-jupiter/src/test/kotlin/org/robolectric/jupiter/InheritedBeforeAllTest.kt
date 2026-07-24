package org.robolectric.jupiter

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class InheritedBeforeAllTest : InheritedBeforeAllBase() {

  @Test
  fun inheritedBeforeAllRuns() {
    assertThat(beforeAllCalled).isTrue()
  }
}

@Suppress("UtilityClassWithPublicConstructor")
open class InheritedBeforeAllBase {
  companion object {
    var beforeAllCalled = false

    @JvmStatic
    @BeforeAll
    fun setupBaseClass() {
      beforeAllCalled = true
    }
  }
}

package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [SandboxSharingStrategy]. */
class SandboxSharingStrategyTest {

  @Test
  fun `all strategies are defined`() {
    val strategies = SandboxSharingStrategy.entries
    assertThat(strategies).hasSize(4)
    assertThat(strategies)
      .containsExactly(
        SandboxSharingStrategy.PER_TEST,
        SandboxSharingStrategy.PER_CLASS,
        SandboxSharingStrategy.PER_SDK,
        SandboxSharingStrategy.GLOBAL,
      )
  }

  @Test
  fun `PER_CLASS is a valid default`() {
    assertThat(SandboxSharingStrategy.PER_CLASS).isNotNull()
  }
}

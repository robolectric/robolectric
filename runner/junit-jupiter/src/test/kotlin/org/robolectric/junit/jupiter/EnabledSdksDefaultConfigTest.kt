package org.robolectric.junit.jupiter

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class EnabledSdksDefaultConfigTest {

  @Test
  fun defaultConfigExecutes_withoutClassLifecycle_one() {
    assertThat(Build.VERSION.SDK_INT).isAtLeast(1)
  }

  @Test
  fun defaultConfigExecutes_withoutClassLifecycle_two() {
    assertThat(Build.VERSION.SDK_INT).isAtLeast(1)
  }
}

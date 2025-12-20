package org.robolectric.junit.platform

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [34])
class EnabledSdksNoClassLifecycleTest {

  @Test
  fun firstMethod_runsWithoutClassLifecycleSetup() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @Test
  fun secondMethod_runsWithoutClassLifecycleSetup() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }
}

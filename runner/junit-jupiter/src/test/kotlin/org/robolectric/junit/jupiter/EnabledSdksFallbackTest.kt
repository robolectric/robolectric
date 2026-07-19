package org.robolectric.junit.jupiter

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class EnabledSdksFallbackTest {

  @Test
  fun fallsBackToConfiguredClassSdkWhenEnabledSdksDoesNotMatch() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(28)
  }

  @Test
  @Config(sdk = [34])
  fun fallsBackToConfiguredMethodSdkWhenEnabledSdksDoesNotMatch() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }
}

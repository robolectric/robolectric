package org.robolectric.jupiter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BeforeAllRobolectricTest {
  companion object {
    lateinit var context: Context

    @JvmStatic
    @BeforeAll
    fun setup() {
      context = ApplicationProvider.getApplicationContext()
    }
  }

  @Test
  fun contextAvailableInTests() {
    assertThat(context).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }
}

package org.robolectric.integrationtests.kotlin

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
class ParameterizedRobolectricTestRunnerTest(private var uri: Uri) {
  @Test
  @Config(manifest = Config.NONE)
  fun parse() {
    val currentUri = Uri.parse("http://host/")
    assertThat(currentUri).isEqualTo(uri)
  }

  companion object {
    @Parameters
    @JvmStatic
    fun getTestData(): Collection<*> {
      val data = arrayOf<Any>(Uri.parse("http://host/"))
      return listOf(data)
    }
  }
}

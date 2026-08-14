package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalRunnerApi::class)
class SystemPropertiesSupportTest {
  private val property = "robolectric.alwaysIncludeVariantMarkersInTestName"

  @After
  fun tearDown() {
    System.clearProperty(property)
  }

  @Test
  fun `alwaysIncludeVariantMarkersInTestName defaults to false`() {
    System.clearProperty(property)

    assertThat(SystemPropertiesSupport.alwaysIncludeVariantMarkersInTestName()).isFalse()
  }

  @Test
  fun `alwaysIncludeVariantMarkersInTestName reads system property`() {
    System.setProperty(property, "true")

    assertThat(SystemPropertiesSupport.alwaysIncludeVariantMarkersInTestName()).isTrue()
  }

  @Test
  fun `formatTestName includes sdk marker for non-last sdk`() {
    val testName =
      SystemPropertiesSupport.formatTestName(
        baseName = "myTest",
        sdkApiLevel = 34,
        alwaysIncludeMarkers = false,
        isLastSdk = false,
      )

    assertThat(testName).isEqualTo("myTest[34]")
  }

  @Test
  fun `formatTestName omits sdk marker for single sdk when disabled`() {
    val testName =
      SystemPropertiesSupport.formatTestName(
        baseName = "myTest",
        sdkApiLevel = 34,
        alwaysIncludeMarkers = false,
        isLastSdk = true,
      )

    assertThat(testName).isEqualTo("myTest")
  }

  @Test
  fun `formatTestName includes sdk marker when always include is enabled`() {
    val testName =
      SystemPropertiesSupport.formatTestName(
        baseName = "myTest",
        sdkApiLevel = 34,
        alwaysIncludeMarkers = true,
        isLastSdk = true,
      )

    assertThat(testName).isEqualTo("myTest[34]")
  }

  @Test
  fun `createSdkSegment formats sdk segment`() {
    assertThat(SystemPropertiesSupport.createSdkSegment(34)).isEqualTo("sdk:34")
  }
}

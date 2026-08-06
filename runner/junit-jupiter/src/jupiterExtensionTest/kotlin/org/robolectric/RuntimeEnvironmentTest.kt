package org.robolectric

import android.content.res.Configuration
import android.view.Surface
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.shadows.ShadowDisplay

@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class RuntimeEnvironmentTest {

  @Test
  fun testSetQualifiersAddPropagateToApplicationResources() {
    RuntimeEnvironment.setQualifiers("+land")
    val app = RuntimeEnvironment.getApplication()
    assertThat(app.resources.configuration.orientation)
      .isEqualTo(Configuration.ORIENTATION_LANDSCAPE)
  }

  @Test
  fun testSetQualifiersReplacePropagateToApplicationResources() {
    RuntimeEnvironment.setQualifiers("land")
    val app = RuntimeEnvironment.getApplication()
    assertThat(app.resources.configuration.orientation)
      .isEqualTo(Configuration.ORIENTATION_LANDSCAPE)
  }

  @Test
  fun testSetFontScale_updatesFontScale() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val displayMetrics = context.resources.displayMetrics

    assertThat(context.resources.configuration.fontScale).isEqualTo(1.0f)
    assertThat(displayMetrics.scaledDensity).isEqualTo(displayMetrics.density)
    assertThat(RuntimeEnvironment.getFontScale()).isEqualTo(1.0f)

    RuntimeEnvironment.setFontScale(1.3f)

    assertThat(context.resources.configuration.fontScale).isEqualTo(1.3f)
    assertThat(displayMetrics.scaledDensity).isEqualTo(displayMetrics.density * 1.3f)
    assertThat(RuntimeEnvironment.getFontScale()).isEqualTo(1.3f)
  }

  @Test
  fun testGetRotation() {
    RuntimeEnvironment.setQualifiers("+land")
    val screenRotation = ShadowDisplay.getDefaultDisplay().rotation
    assertThat(screenRotation).isEqualTo(Surface.ROTATION_90)
  }

  @Test
  fun setQualifiers_withResultFromGetQualifiers() {
    // Calling this should not cause an exception, e.g. API level mismatch.
    RuntimeEnvironment.setQualifiers(RuntimeEnvironment.getQualifiers())
  }

  @Test
  @Config(qualifiers = "w100dp-h200dp-port")
  fun setQualifiers_modifyWidthToGreaterThanHeight_setsOrientationToLandscape() {
    RuntimeEnvironment.setQualifiers("+w300dp")

    assertThat(
        ApplicationProvider.getApplicationContext<android.app.Application>()
          .resources
          .configuration
          .orientation
      )
      .isEqualTo(Configuration.ORIENTATION_LANDSCAPE)
  }
}

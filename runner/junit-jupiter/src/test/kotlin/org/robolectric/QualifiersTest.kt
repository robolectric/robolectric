package org.robolectric

import android.app.Activity
import android.content.res.Configuration
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension

@ExtendWith(RobolectricExtension::class)
class QualifiersTest {

  @Test
  @Config(sdk = [26])
  fun testDefaultQualifiers() {
    assertThat(RuntimeEnvironment.getQualifiers())
      .isEqualTo(
        "en-rUS-ldltr-sw320dp-w320dp-h470dp-normal-notlong-notround-nowidecg-lowdr-port-" +
          "notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-470x320"
      )
  }

  @Test
  @Config(qualifiers = "land")
  fun orientation() {
    val resources = RuntimeEnvironment.getApplication().resources
    assertThat(resources.configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE)
  }

  @Test
  @Config(qualifiers = "fr")
  fun shouldBeFrench() {
    val resources = RuntimeEnvironment.getApplication().resources
    val locale =
      if (android.os.Build.VERSION.SDK_INT >= 24) {
        resources.configuration.locales[0]
      } else {
        @Suppress("DEPRECATION") resources.configuration.locale
      }
    assertThat(locale.language).isEqualTo("fr")
  }

  @Test
  @Config(qualifiers = "sw720dp")
  fun inflateLayout_overridesTo_sw720dp() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      val resources = activity.resources
      assertThat(resources.configuration.smallestScreenWidthDp).isEqualTo(720)
    }
  }
}

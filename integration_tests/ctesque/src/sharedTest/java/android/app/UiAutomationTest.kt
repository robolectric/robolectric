package android.app

import android.content.res.Configuration
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
import android.view.Surface
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument
import org.robolectric.testapp.TestActivity

@Suppress("DEPRECATION")
@DoNotInstrument
@Config(minSdk = JELLY_BEAN_MR2)
@RunWith(AndroidJUnit4::class)
class UiAutomationTest {
  @Test
  fun setRotation_freeze90_isLandscape() {
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90)

    ActivityScenario.launch(TestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val display = activity.windowManager.defaultDisplay
        val configuration = activity.resources.configuration
        assertThat(display.rotation).isEqualTo(Surface.ROTATION_90)
        assertThat(display.width).isGreaterThan(display.height)
        assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE)
        assertThat(configuration.screenWidthDp).isGreaterThan(configuration.screenHeightDp)
      }
    }
  }

  @Test
  fun setRotation_freeze180_isPortrait() {
    val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_180)

    ActivityScenario.launch(TestActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        val display = activity.windowManager.defaultDisplay
        val configuration = activity.resources.configuration
        assertThat(display.rotation).isEqualTo(Surface.ROTATION_180)
        assertThat(display.width).isLessThan(display.height)
        assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT)
        assertThat(configuration.screenWidthDp).isLessThan(configuration.screenHeightDp)
      }
    }
  }
}
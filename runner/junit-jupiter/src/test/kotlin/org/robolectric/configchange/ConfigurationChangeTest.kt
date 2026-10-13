package org.robolectric.configchange

import android.content.res.Configuration
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.testhelpers.ConfigTestActivity

/**
 * Tests for orientation changes and device rotation.
 *
 * These tests verify that activities properly handle configuration changes, including:
 * - Activity recreation via Robolectric's activity lifecycle methods
 * - Lifecycle method calls
 * - State preservation using onSaveInstanceState/onRestoreInstanceState
 *
 * Note: These tests use Robolectric's buildActivity() API rather than ActivityScenario to avoid
 * androidx.lifecycle dependency issues in the runner module tests.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [28]) // Use SDK 28 (Android 9) for better support
class ConfigurationChangeTest {

  @Test
  fun testOrientationChange_recreatesActivity() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()
    val originalHashCode = System.identityHashCode(activity)
    activity.saveOriginalInstanceId(originalHashCode)

    // Trigger recreation by simulating configuration change
    val bundle = android.os.Bundle()
    controller.saveInstanceState(bundle).pause().stop().destroy()

    // Create new activity with saved state
    val newActivity =
      Robolectric.buildActivity(ConfigTestActivity::class.java)
        .create(bundle)
        .start()
        .resume()
        .get()

    val newHashCode = System.identityHashCode(newActivity)

    // Verify it's a different instance
    assertThat(newHashCode).isNotEqualTo(originalHashCode)
    // Verify the saved ID matches
    assertThat(newActivity.savedOriginalInstanceId).isEqualTo(originalHashCode)
    // Verify recreation flag
    assertThat(newActivity.wasRecreated).isTrue()
  }

  @Test
  fun testOrientationChange_callsOnSaveInstanceState() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()
    activity.transcript.clear()

    // Trigger recreation
    val bundle = android.os.Bundle()
    controller.saveInstanceState(bundle)
    controller.pause().stop().destroy()

    // Verify onSaveInstanceState was called by checking that state was saved
    assertThat(bundle.isEmpty).isFalse()
  }

  @Test
  fun testOrientationChange_restoresState() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()
    val testString = "Test Data"
    val testInt = 42

    activity.savedStringState = testString
    activity.savedIntState = testInt

    // Trigger recreation
    val bundle = android.os.Bundle()
    controller.saveInstanceState(bundle).pause().stop().destroy()

    // Create new activity with saved state
    val newActivity =
      Robolectric.buildActivity(ConfigTestActivity::class.java)
        .create(bundle)
        .start()
        .resume()
        .get()

    // Verify state was restored
    assertThat(newActivity.savedStringState).isEqualTo(testString)
    assertThat(newActivity.savedIntState).isEqualTo(testInt)
  }

  @Test
  fun testOrientationChange_multipleRotations() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()
    activity.saveOriginalInstanceId(123)

    // First recreation
    val bundle1 = android.os.Bundle()
    controller.saveInstanceState(bundle1).pause().stop().destroy()

    val controller2 =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create(bundle1).start().resume()

    val activity2 = controller2.get()

    assertThat(activity2.creationCount).isEqualTo(2)
    assertThat(activity2.savedOriginalInstanceId).isEqualTo(123)

    // Second recreation
    val bundle2 = android.os.Bundle()
    controller2.saveInstanceState(bundle2).pause().stop().destroy()

    val activity3 =
      Robolectric.buildActivity(ConfigTestActivity::class.java)
        .create(bundle2)
        .start()
        .resume()
        .get()

    assertThat(activity3.creationCount).isEqualTo(3)
    assertThat(activity3.savedOriginalInstanceId).isEqualTo(123)
  }

  @Test
  fun testOrientationChange_tracksLifecycleMethods() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()

    // Verify initial lifecycle
    assertThat(activity.transcript).contains("onCreate")
    assertThat(activity.transcript).contains("onStart")
    assertThat(activity.transcript).contains("onResume")

    activity.transcript.clear()

    // Trigger recreation
    val bundle = android.os.Bundle()
    controller.saveInstanceState(bundle).pause().stop().destroy()

    // Verify destruction lifecycle
    assertThat(activity.transcript).contains("onPause")
    assertThat(activity.transcript).contains("onStop")
    assertThat(activity.transcript).contains("onDestroy")
  }

  @Test
  fun testConfigurationChange_detectsOrientationChange() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()

    // Verify starting in portrait (default)
    assertThat(activity.isPortrait()).isTrue()

    activity.transcript.clear()

    // Change configuration to landscape
    val newConfig = Configuration(activity.resources.configuration)
    newConfig.orientation = Configuration.ORIENTATION_LANDSCAPE
    activity.onConfigurationChanged(newConfig)

    // Verify onConfigurationChanged was called
    assertThat(activity.transcript).contains("onConfigurationChanged")
  }
}

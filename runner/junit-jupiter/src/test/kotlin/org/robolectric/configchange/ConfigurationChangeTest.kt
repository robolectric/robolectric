package org.robolectric.configchange

import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.testhelpers.ConfigTestActivity

/** Tests that an activity recreated as for a configuration change gets back its saved state. */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class ConfigurationChangeTest {

  @Test
  fun testOrientationChange_restoresState() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()
    val testString = "Test Data"
    val testInt = 42

    activity.savedStringState = testString
    activity.savedIntState = testInt

    val bundle = Bundle()
    controller.saveInstanceState(bundle).pause().stop().destroy()

    val newActivity =
      Robolectric.buildActivity(ConfigTestActivity::class.java)
        .create(bundle)
        .start()
        .resume()
        .get()

    assertThat(newActivity.savedStringState).isEqualTo(testString)
    assertThat(newActivity.savedIntState).isEqualTo(testInt)
  }

  @Test
  fun testOrientationChange_multipleRotations() {
    val controller =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create().start().resume()

    val activity = controller.get()
    activity.savedOriginalInstanceId = 123

    val bundle1 = Bundle()
    controller.saveInstanceState(bundle1).pause().stop().destroy()

    val controller2 =
      Robolectric.buildActivity(ConfigTestActivity::class.java).create(bundle1).start().resume()

    val activity2 = controller2.get()

    assertThat(activity2.creationCount).isEqualTo(2)
    assertThat(activity2.savedOriginalInstanceId).isEqualTo(123)

    val bundle2 = Bundle()
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
}

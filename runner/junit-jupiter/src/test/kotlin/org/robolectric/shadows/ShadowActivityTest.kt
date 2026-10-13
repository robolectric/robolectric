package org.robolectric.shadows

import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.testapp.TestActivity

@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class ShadowActivityTest {

  @Test
  fun shouldReportDestroyedStatus(controller: ActivityController<TestActivity>) {
    val activity = controller.get()
    controller.destroy()
    assertThat(activity.isDestroyed).isTrue()
  }

  @Test
  fun startActivity_shouldDelegateToStartActivityForResult(
    controller: ActivityController<TestActivity>
  ) {
    val activity = controller.setup().get()
    val intent = Intent().setType("image/*")
    activity.startActivity(intent)

    val startedIntent = shadowOf(activity).nextStartedActivity
    assertThat(startedIntent).isEqualTo(intent)
  }

  @Test
  fun shouldRetrievePackageName(context: Context) {
    // Verify context package name matches the application package name
    assertThat(context.packageName).isEqualTo(RuntimeEnvironment.getApplication().packageName)
  }
}

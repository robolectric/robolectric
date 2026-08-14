package org.robolectric.shadows

import android.app.Activity
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(sdk = [34])
class ShadowActivityTest {

  @Test
  fun shouldReportDestroyedStatus() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.get()
      controller.destroy()
      assertThat(activity.isDestroyed).isTrue()
    }
  }

  @Test
  fun startActivity_shouldDelegateToStartActivityForResult() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      val intent = Intent().setType("image/*")
      activity.startActivity(intent)

      val startedIntent = shadowOf(activity).nextStartedActivity
      assertThat(startedIntent).isEqualTo(intent)
    }
  }
}

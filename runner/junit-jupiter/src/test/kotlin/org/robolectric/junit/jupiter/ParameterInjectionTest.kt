package org.robolectric.junit.jupiter

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.android.controller.ActivityController
import org.robolectric.android.controller.ServiceController

/** Tests the parameters that the Android environment provides to test methods. */
@ExtendWith(RobolectricExtension::class)
class ParameterInjectionTest {
  @Test
  fun testActivityControllerInjection(controller: ActivityController<Activity>) {
    val activity = controller.setup().get()
    assertThat(activity.isFinishing).isFalse()
  }

  @Test
  fun testServiceControllerInjection(controller: ServiceController<TestService>) {
    assertThat(controller.create().get()).isInstanceOf(TestService::class.java)
  }

  class TestService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
  }
}

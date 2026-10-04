package org.robolectric.android.controller

import android.app.Activity
import java.util.concurrent.atomic.AtomicReference
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Robolectric
import org.robolectric.junit.jupiter.RobolectricExtension

@ExtendWith(RobolectricExtension::class)
class ActivityControllerRecreateTest {

  @Test
  fun recreateFromSharedController_doesNotThrow_1() {
    sharedController().recreate()
  }

  @Test
  fun recreateFromSharedController_doesNotThrow_2() {
    sharedController().recreate()
  }

  private fun sharedController(): ActivityController<Activity> {
    return createdActivity.updateAndGet { existing ->
      existing ?: Robolectric.buildActivity(Activity::class.java).create()
    }
  }

  companion object {
    private val createdActivity = AtomicReference<ActivityController<Activity>>()
  }
}

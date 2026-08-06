package org.robolectric.android.controller

import android.app.Activity
import java.util.concurrent.atomic.AtomicReference
import org.junit.Test
import org.robolectric.Robolectric

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

package org.robolectric.integrationtests

import android.app.Activity
import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.Robolectric

/**
 * Integration tests to verify that multiple engines can coexist on the classpath without conflicts.
 *
 * This test class uses JUnit 4 (@Test) annotations which should be picked up by the junit-platform
 * engine.
 */
class MultiEngineCoexistenceTest {

  /** JUnit 4 style test - should be picked up by junit-platform engine. */
  @Test
  fun junit4TestWithBuildActivity() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      assertThat(activity).isNotNull()
      assertThat(activity.isFinishing).isFalse()
    }
  }

  /** JUnit 4 test with parameter injection. */
  @Test
  fun junit4TestWithParameterInjection(context: Context) {
    assertThat(context).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }
}

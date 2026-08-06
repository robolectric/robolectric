package org.robolectric

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.android.controller.ActivityController

/**
 * Comprehensive tests for parameter injection in JUnit Platform engine.
 *
 * Verifies that Context, Application, and ActivityController can be properly injected as test
 * method parameters.
 */
class ParameterInjectionTest {

  @Test
  fun testContextInjection(context: Context) {
    assertThat(context).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }

  @Test
  fun testApplicationInjection(application: Application) {
    assertThat(application).isNotNull()
    assertThat(application.packageName).isNotEmpty()
  }

  @Test
  fun testActivityControllerInjection(controller: ActivityController<Activity>) {
    assertThat(controller).isNotNull()
    val activity = controller.setup().get()
    assertThat(activity).isNotNull()
    assertThat(activity.isFinishing).isFalse()
  }

  @Test
  fun testMultipleParametersInjection(context: Context, application: Application) {
    assertThat(context).isNotNull()
    assertThat(application).isNotNull()
    assertThat(context.applicationContext).isSameInstanceAs(application)
  }

  @Test
  fun testMixedParametersWithActivityController(
    context: Context,
    activityController: ActivityController<Activity>,
  ) {
    assertThat(context).isNotNull()
    assertThat(activityController).isNotNull()

    val activity = activityController.setup().get()
    assertThat(activity.applicationContext).isSameInstanceAs(context.applicationContext)
  }
}

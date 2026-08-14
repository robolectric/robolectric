package org.robolectric

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.android.controller.ActivityController
import org.robolectric.junit.jupiter.RobolectricExtension

/**
 * Comprehensive tests for parameter injection in JUnit Jupiter engine.
 *
 * Verifies that Context, Application, and ActivityController can be properly injected as test
 * method parameters.
 */
@ExtendWith(RobolectricExtension::class)
class JupiterParameterInjectionTest {

  private var beforeEachCalled = false

  @BeforeEach
  fun setUp() {
    beforeEachCalled = true
  }

  @AfterEach
  fun tearDown() {
    assertThat(beforeEachCalled).isTrue()
  }

  @Test
  fun testContextInjection(context: Context) {
    assertThat(beforeEachCalled).isTrue()
    assertThat(context).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }

  @Test
  fun testApplicationInjection(application: Application) {
    assertThat(beforeEachCalled).isTrue()
    assertThat(application).isNotNull()
    assertThat(application.packageName).isNotEmpty()
  }

  @Test
  fun testActivityControllerInjection(controller: ActivityController<Activity>) {
    assertThat(beforeEachCalled).isTrue()
    assertThat(controller).isNotNull()
    val activity = controller.setup().get()
    assertThat(activity).isNotNull()
    assertThat(activity.isFinishing).isFalse()
  }

  @Test
  fun testMultipleParametersInjection(context: Context, application: Application) {
    assertThat(beforeEachCalled).isTrue()
    assertThat(context).isNotNull()
    assertThat(application).isNotNull()
    assertThat(context.applicationContext).isSameInstanceAs(application)
  }
}

package org.robolectric

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowView

@Config(sdk = [34])
class RobolectricTest {

  private val context = ApplicationProvider.getApplicationContext<android.app.Application>()

  @Test
  fun idleMainLooper_executesScheduledTasks() {
    val wasRun = booleanArrayOf(false)
    Handler(Looper.getMainLooper()).postDelayed({ wasRun[0] = true }, 2000)

    assertThat(wasRun[0]).isFalse()
    ShadowLooper.idleMainLooper(1999, TimeUnit.MILLISECONDS)
    assertThat(wasRun[0]).isFalse()
    ShadowLooper.idleMainLooper(1, TimeUnit.MILLISECONDS)
    assertThat(wasRun[0]).isTrue()
  }

  @Test
  fun buildActivity_returnsAVisibleActivity() {
    Robolectric.buildActivity(LifeCycleActivity::class.java).use { controller ->
      val activity = controller.setup().get()

      assertThat(activity.isCreated).isTrue()
      assertThat(activity.isStarted).isTrue()
      assertThat(activity.isResumed).isTrue()
      assertThat(activity.isVisible()).isTrue()
    }
  }

  @Test
  fun clickOn_shouldThrowIfViewIsDisabled() {
    val view = View(context)
    view.isEnabled = false
    try {
      ShadowView.clickOn(view)
      throw AssertionError("Expected RuntimeException")
    } catch (e: RuntimeException) {
      // Expected
    }
  }

  @Test
  fun clickOn_shouldCallClickListenerWhenEnabled() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      val view = View(activity)
      view.isEnabled = true
      activity.setContentView(view)
      var clicked = false
      view.setOnClickListener { clicked = true }

      ShadowView.clickOn(view)
      assertThat(clicked).isTrue()
    }
  }

  @Test
  fun checkActivities_shouldSetValueOnShadowApplication() {
    shadowOf(RuntimeEnvironment.getApplication()).checkActivities(true)
    try {
      context.startActivity(Intent("i.dont.exist.activity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
      throw AssertionError("Expected ActivityNotFoundException")
    } catch (e: ActivityNotFoundException) {
      // Expected
    }
  }

  private class LifeCycleActivity : Activity() {
    var isCreated: Boolean = false
    var isStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      isCreated = true
    }

    override fun onStart() {
      super.onStart()
      isStarted = true
    }

    fun isVisible(): Boolean {
      return window.decorView.windowToken != null
    }
  }
}

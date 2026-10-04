package org.robolectric.android.controller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.shadows.ShadowWindowManagerImpl

@ExtendWith(RobolectricExtension::class)
class ActivityControllerTest {

  @Test
  fun canCreateActivityNotListedInManifest() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      assertThat(activity).isNotNull()
    }
  }

  @Test
  fun visible_addsTheDecorViewToTheWindowManager(controller: ActivityController<Activity>) {
    controller.create().visible()
    assertThat(controller.get().window.decorView.parent).isNotNull()
  }

  @Test
  fun setup_callsLifecycleMethods() {
    val transcript = mutableListOf<String>()
    val controller = Robolectric.buildActivity(LifecycleActivity::class.java)
    (controller.get() as LifecycleActivity).transcript = transcript

    controller.setup()
    assertThat(transcript).containsExactly("onCreate", "onStart", "onResume")
  }

  @Test
  fun pauseAndResume_callsLifecycleMethods() {
    val transcript = mutableListOf<String>()
    val controller = Robolectric.buildActivity(LifecycleActivity::class.java)
    val activity = controller.get() as LifecycleActivity
    activity.transcript = transcript

    controller.setup()
    transcript.clear()

    controller.pause()
    assertThat(transcript).contains("onPause")

    controller.resume()
    assertThat(transcript).contains("onResume")
  }

  @Test
  fun stop_callsOnStop() {
    val transcript = mutableListOf<String>()
    val controller = Robolectric.buildActivity(LifecycleActivity::class.java)
    val activity = controller.get() as LifecycleActivity
    activity.transcript = transcript

    controller.setup()
    transcript.clear()

    controller.pause().stop()
    assertThat(transcript).contains("onStop")
  }

  @Test
  fun activityWithIntent_receivesIntent() {
    val intent = Intent(Intent.ACTION_VIEW).apply { putExtra("key", "value") }
    val controller = Robolectric.buildActivity(Activity::class.java, intent)

    val activity = controller.create().get()
    assertThat(activity.intent.action).isEqualTo(Intent.ACTION_VIEW)
    assertThat(activity.intent.getStringExtra("key")).isEqualTo("value")
  }

  @Test
  fun destroy_cleansUpWindowManagerState() {
    val controller = Robolectric.buildActivity(Activity::class.java)
    val windowManager = controller.get().windowManager
    val shadowWindowManager = shadowOf(windowManager) as ShadowWindowManagerImpl

    controller.create().start().resume().visible()
    val decorView = controller.get().window.decorView
    assertThat(shadowWindowManager.views).contains(decorView)

    controller.pause().stop().destroy()
    assertThat(shadowWindowManager.views).doesNotContain(decorView)
  }

  @Test
  fun close_transitionsActivityStateToDestroyed() {
    val controller = Robolectric.buildActivity(Activity::class.java).create().start().resume()
    val activity = controller.get()
    controller.close()
    assertThat(activity.isDestroyed).isTrue()
  }

  @Test
  fun newIntent_deliversIntentToActivity() {
    val controller = Robolectric.buildActivity(IntentAwareActivity::class.java).create()
    controller.newIntent(Intent(Intent.ACTION_VIEW))
    assertThat(controller.get().lastAction).isEqualTo(Intent.ACTION_VIEW)
  }

  @Test
  fun userLeaving_callsOnUserLeaveHint() {
    val controller = Robolectric.buildActivity(IntentAwareActivity::class.java).create()
    controller.userLeaving()
    assertThat(controller.get().userLeaveHintCalled).isTrue()
  }

  private class LifecycleActivity : Activity() {
    var transcript = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      transcript.add("onCreate")
    }

    override fun onStart() {
      super.onStart()
      transcript.add("onStart")
    }

    override fun onResume() {
      super.onResume()
      transcript.add("onResume")
    }

    override fun onPause() {
      super.onPause()
      transcript.add("onPause")
    }

    override fun onStop() {
      super.onStop()
      transcript.add("onStop")
    }
  }

  private class IntentAwareActivity : Activity() {
    var lastAction: String? = null
    var userLeaveHintCalled: Boolean = false

    override fun onNewIntent(intent: Intent?) {
      super.onNewIntent(intent)
      lastAction = intent?.action
    }

    override fun onUserLeaveHint() {
      super.onUserLeaveHint()
      userLeaveHintCalled = true
    }
  }
}

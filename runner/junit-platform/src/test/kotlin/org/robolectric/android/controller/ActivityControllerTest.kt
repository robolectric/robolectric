package org.robolectric.android.controller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.Robolectric

class ActivityControllerTest {

  @Test
  fun canCreateActivityNotListedInManifest() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      assertThat(activity).isNotNull()
    }
  }

  @Test
  fun visible_addsTheDecorViewToTheWindowManager() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      controller.create().visible()
      assertThat(controller.get().window.decorView.parent).isNotNull()
    }
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
}

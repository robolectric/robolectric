package org.robolectric.android.controller

import android.app.IntentService
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@Config(sdk = [34])
class IntentServiceControllerTest {

  @Test
  fun onBind_setsIntentComponent() {
    transcript.clear()
    val componentName =
      ComponentName(
        RuntimeEnvironment.getApplication().packageName,
        MyIntentService::class.java.name,
      )
    val service = Robolectric.buildIntentService(MyIntentService::class.java).create().bind().get()
    assertThat(service.boundIntent?.component).isEqualTo(componentName)
  }

  @Test
  fun onStartCommand_setsIntentComponent() {
    transcript.clear()
    val componentName =
      ComponentName(
        RuntimeEnvironment.getApplication().packageName,
        MyIntentService::class.java.name,
      )
    val service =
      Robolectric.buildIntentService(MyIntentService::class.java).create().startCommand(3, 4).get()
    assertThat(service.startIntent?.component).isEqualTo(componentName)
  }

  @Test
  fun whenMainLooperPaused_createStillInvokesCallbacks() {
    transcript.clear()
    ShadowLooper.shadowMainLooper().pause()
    Robolectric.buildIntentService(MyIntentService::class.java).create()
    assertThat(transcript).contains("finishedOnCreate")
    assertThat(transcript).contains("onCreate")
  }

  @Test
  fun create_invokesOnCreateCallbacks() {
    transcript.clear()
    Robolectric.buildIntentService(MyIntentService::class.java).create()
    assertThat(transcript).containsAtLeast("finishedOnCreate", "onCreate")
  }

  class MyIntentService : IntentService("ThreadName") {
    private val handler = Handler(android.os.Looper.getMainLooper())

    var boundIntent: Intent? = null
    var startIntent: Intent? = null

    override fun onBind(intent: Intent?): IBinder? {
      boundIntent = intent
      transcribeWhilePaused("onBind")
      transcript.add("finishedOnBind")
      return null
    }

    override fun onHandleIntent(intent: Intent?) {
      startIntent = intent
      transcribeWhilePaused("onHandleIntent")
      transcript.add("finishedOnHandleIntent")
    }

    override fun onCreate() {
      super.onCreate()
      transcribeWhilePaused("onCreate")
      transcript.add("finishedOnCreate")
    }

    private fun transcribeWhilePaused(event: String) {
      handler.post { transcript.add(event) }
    }
  }

  companion object {
    private val transcript = mutableListOf<String>()
  }
}

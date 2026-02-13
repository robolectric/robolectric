package org.robolectric.android.controller

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension

@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class ServiceControllerTest {

  @Test
  fun onBindShouldSetIntent() {
    transcript.clear()
    val controller = Robolectric.buildService(MyService::class.java)
    val componentName =
      ComponentName(RuntimeEnvironment.getApplication().packageName, MyService::class.java.name)

    val myService = controller.create().bind().get()
    assertThat(myService.boundIntent).isNotNull()
    assertThat(myService.boundIntent?.component).isEqualTo(componentName)
  }

  @Test
  fun onStartCommandShouldSetIntentAndFlags() {
    transcript.clear()
    val controller = Robolectric.buildService(MyService::class.java)
    val componentName =
      ComponentName(RuntimeEnvironment.getApplication().packageName, MyService::class.java.name)

    val myService = controller.create().startCommand(3, 4).get()
    assertThat(myService.startIntent).isNotNull()
    assertThat(myService.startIntent?.component).isEqualTo(componentName)
    assertThat(myService.startFlags).isEqualTo(3)
    assertThat(myService.startId).isEqualTo(4)
  }

  @Test
  fun onBindShouldSetIntentComponentWithCustomIntentWithoutComponentSet() {
    transcript.clear()
    val componentName =
      ComponentName(RuntimeEnvironment.getApplication().packageName, MyService::class.java.name)

    val myService =
      Robolectric.buildService(MyService::class.java, Intent(Intent.ACTION_VIEW)).bind().get()
    assertThat(myService.boundIntent?.action).isEqualTo(Intent.ACTION_VIEW)
    assertThat(myService.boundIntent?.component).isEqualTo(componentName)
  }

  @Test
  fun shouldSetIntentForGivenServiceInstance() {
    transcript.clear()
    val serviceController = ServiceController.of(MyService(), null).bind()
    assertThat(serviceController.get().boundIntent).isNotNull()
  }

  @Test
  fun unbind_callsUnbindWhilePaused() {
    transcript.clear()
    val controller = Robolectric.buildService(MyService::class.java)

    controller.create().bind().unbind()
    assertThat(transcript).containsAtLeast("finishedOnUnbind", "onUnbind")
  }

  @Test
  fun rebind_callsRebindWhilePaused() {
    transcript.clear()
    val controller = Robolectric.buildService(MyService::class.java)

    controller.create().bind().unbind().bind().rebind()
    assertThat(transcript).containsAtLeast("finishedOnRebind", "onRebind")
  }

  @Test
  fun destroy_callsOnDestroyWhilePaused() {
    transcript.clear()
    val controller = Robolectric.buildService(MyService::class.java)

    controller.create().destroy()
    assertThat(transcript).containsAtLeast("finishedOnDestroy", "onDestroy")
  }

  @Test
  fun bind_callsOnBindWhilePaused() {
    transcript.clear()
    val controller = Robolectric.buildService(MyService::class.java)

    controller.create().bind()
    assertThat(transcript).containsAtLeast("finishedOnBind", "onBind")
  }

  @Test
  fun startCommand_callsOnStartCommandWhilePaused() {
    transcript.clear()
    val controller = Robolectric.buildService(MyService::class.java)

    controller.create().startCommand(1, 2)
    assertThat(transcript).containsAtLeast("finishedOnStartCommand", "onStartCommand")
  }

  class MyService : Service() {
    private val handler = Handler(Looper.getMainLooper())

    var boundIntent: Intent? = null
    var reboundIntent: Intent? = null
    var startIntent: Intent? = null
    var startFlags: Int = 0
    var startId: Int = 0
    var unboundIntent: Intent? = null

    override fun onBind(intent: Intent?): IBinder? {
      boundIntent = intent
      transcribeWhilePaused("onBind")
      transcript.add("finishedOnBind")
      return null
    }

    override fun onCreate() {
      super.onCreate()
      transcribeWhilePaused("onCreate")
      transcript.add("finishedOnCreate")
    }

    override fun onDestroy() {
      super.onDestroy()
      transcribeWhilePaused("onDestroy")
      transcript.add("finishedOnDestroy")
    }

    override fun onRebind(intent: Intent?) {
      reboundIntent = intent
      transcribeWhilePaused("onRebind")
      transcript.add("finishedOnRebind")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      startIntent = intent
      startFlags = flags
      this.startId = startId
      transcribeWhilePaused("onStartCommand")
      transcript.add("finishedOnStartCommand")
      return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
      unboundIntent = intent
      transcribeWhilePaused("onUnbind")
      transcript.add("finishedOnUnbind")
      return false
    }

    private fun transcribeWhilePaused(event: String) {
      runOnUiThread { transcript.add(event) }
    }

    private fun runOnUiThread(action: Runnable) {
      handler.post(action)
    }
  }

  companion object {
    private val transcript = mutableListOf<String>()
  }
}

package org.robolectric

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

/**
 * Minimal integration test suite for JUnit Platform engine.
 *
 * This test verifies that the Robolectric JUnit Platform integration works correctly. It focuses on
 * testing the integration layer, not Android APIs themselves.
 */
@Config(sdk = [34])
class PlatformEngineIntegrationTest {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext()

  // === Engine Integration Tests ===

  @Test
  fun sandboxExecution_runsTestsInCorrectSdk() {
    // Verify tests run in configured sandbox with correct SDK
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
    assertThat(context).isNotNull()
  }

  @Test
  fun shadows_areProperlyConfigured() {
    // Verify Robolectric shadows are available and functional
    val looper = Looper.getMainLooper()
    val shadowLooper = shadowOf(looper)
    assertThat(shadowLooper).isNotNull()

    var executed = false
    Handler(looper).post { executed = true }
    shadowLooper.idle()
    assertThat(executed).isTrue()
  }

  // === Configuration Tests ===

  @Test
  @Config(sdk = [33])
  fun config_annotation_isRespected() {
    // Verify @Config annotation affects sandbox initialization
    assertThat(Build.VERSION.SDK_INT).isEqualTo(33)
  }

  // === Exception Handling Tests ===

  @Test
  fun exceptions_areProperlyCaught() {
    // Verify exceptions in tests are caught and reported correctly
    assertThrows(RuntimeException::class.java) { throw RuntimeException("test exception") }
  }

  // === Component Controller Tests ===

  @Test
  fun activityController_works() {
    // Verify ActivityController integration with buildActivity pattern
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      assertThat(activity).isNotNull()
      assertThat(activity.isDestroyed).isFalse()
    }
  }

  @Test
  fun serviceController_works() {
    // Verify ServiceController integration
    val service = Robolectric.setupService(TestService::class.java)
    assertThat(service).isNotNull()
  }

  // === Android Framework Integration Tests ===

  @Test
  fun sharedPreferences_persistsData() {
    // Verify SharedPreferences works in sandbox
    val prefs = context.getSharedPreferences("test", Context.MODE_PRIVATE)
    prefs.edit().putString("key", "value").commit()

    val prefs2 = context.getSharedPreferences("test", Context.MODE_PRIVATE)
    assertThat(prefs2.getString("key", null)).isEqualTo("value")
  }

  @Test
  fun systemServices_areAvailable() {
    // Verify system services are properly mocked
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
    assertThat(activityManager).isInstanceOf(android.app.ActivityManager::class.java)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
    assertThat(alarmManager).isInstanceOf(android.app.AlarmManager::class.java)
  }

  @Test
  fun broadcastReceiver_receivesIntents() {
    // Verify BroadcastReceiver integration
    var received = false
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
          received = true
        }
      }

    context.registerReceiver(receiver, IntentFilter("test.action.CUSTOM"))
    context.sendBroadcast(Intent("test.action.CUSTOM"))
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(received).isTrue()
    context.unregisterReceiver(receiver)
  }

  @Test
  fun looper_schedulesCallbacks() {
    // Verify Looper/Handler integration
    var callbackExecuted = false
    Handler(Looper.getMainLooper()).postDelayed({ callbackExecuted = true }, 100)

    assertThat(callbackExecuted).isFalse()
    ShadowLooper.idleMainLooper(100, java.util.concurrent.TimeUnit.MILLISECONDS)
    assertThat(callbackExecuted).isTrue()
  }

  @Test
  fun application_contextIsAvailable() {
    // Verify application context is properly initialized
    assertThat(context.packageName).isNotEmpty()
    assertThat(context.applicationContext).isSameInstanceAs(context)
  }

  // === Parameter Injection Tests ===

  @Test
  fun parameterInjection_contextWorks(context: Context) {
    // Verify Context parameter injection
    assertThat(context).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }

  @Test
  fun parameterInjection_applicationWorks(application: android.app.Application) {
    // Verify Application parameter injection
    assertThat(application).isNotNull()
    assertThat(application).isSameInstanceAs(ApplicationProvider.getApplicationContext<Context>())
  }

  @Test
  fun parameterInjection_activityControllerWorks(
    controller: org.robolectric.android.controller.ActivityController<Activity>
  ) {
    // Verify ActivityController parameter injection
    val activity = controller.setup().get()
    assertThat(activity).isNotNull()
    assertThat(activity.isDestroyed).isFalse()
  }

  @Test
  fun parameterInjection_serviceControllerWorks(
    controller: org.robolectric.android.controller.ServiceController<TestService>
  ) {
    // Verify ServiceController parameter injection
    val service = controller.create().get()
    assertThat(service).isNotNull()
  }

  @Test
  fun parameterInjection_multipleParametersWork(
    context: Context,
    activityController: org.robolectric.android.controller.ActivityController<Activity>,
  ) {
    // Verify multiple parameters can be injected
    assertThat(context).isNotNull()
    assertThat(activityController).isNotNull()

    val activity = activityController.setup().get()
    assertThat(activity.applicationContext).isSameInstanceAs(context)
  }

  // === Test Service for ServiceController test ===

  class TestService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
  }
}

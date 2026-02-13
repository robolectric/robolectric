package org.robolectric

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.shadows.ShadowLooper

/**
 * Minimal integration test suite for JUnit Jupiter extension.
 *
 * This test verifies that the Robolectric JUnit Jupiter integration works correctly, including the
 * RobolectricExtension for parameter injection and lifecycle management.
 */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class JupiterExtensionIntegrationTest {

  private var setupComplete: Boolean = false

  @BeforeEach
  fun setup() {
    setupComplete = true
  }

  @AfterEach
  fun teardown() {
    setupComplete = false
  }

  // === Lifecycle Tests ===

  @Test
  fun lifecycle_beforeEachExecutes() {
    // Verify JUnit Jupiter lifecycle integration
    assertThat(setupComplete).isTrue()
  }

  // === Parameter Injection Tests ===

  @Test
  fun parameterInjection_contextWorks(context: Context) {
    // Verify Context parameter injection
    assertThat(context).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }

  @Test
  fun parameterInjection_applicationWorks(application: Application) {
    // Verify Application parameter injection
    assertThat(application).isNotNull()
    assertThat(application).isEqualTo(RuntimeEnvironment.getApplication())
  }

  @Test
  fun parameterInjection_activityControllerWorks(controller: ActivityController<Activity>) {
    // Verify ActivityController parameter injection
    val activity = controller.setup().get()
    assertThat(activity).isNotNull()
    assertThat(activity.isFinishing).isFalse()
  }

  @Test
  fun parameterInjection_serviceControllerWorks(controller: ServiceController<TestService>) {
    // Verify ServiceController parameter injection
    val service = controller.create().get()
    assertThat(service).isNotNull()
  }

  @Test
  fun parameterInjection_multipleParametersWork(
    context: Context,
    activityController: ActivityController<Activity>,
  ) {
    // Verify multiple parameters can be injected
    assertThat(context).isNotNull()
    assertThat(activityController).isNotNull()

    val activity = activityController.setup().get()
    assertThat(activity.applicationContext).isEqualTo(context)
  }

  @Test
  fun parameterInjection_manualContextWorks() {
    // Verify manual context access still works (backward compatibility)
    val context = RuntimeEnvironment.getApplication()
    assertThat(context).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }

  @Test
  fun activityController_manualWorks() {
    // Verify manual ActivityController creation works (backward compatibility)
    val controller = Robolectric.buildActivity(Activity::class.java)
    val activity = controller.setup().get()
    assertThat(activity).isNotNull()
    assertThat(activity.isFinishing).isFalse()
  }

  @Test
  fun serviceController_manualWorks() {
    // Verify manual ServiceController creation works (backward compatibility)
    val controller = Robolectric.buildService(TestService::class.java)
    val service = controller.create().get()
    assertThat(service).isNotNull()
  }

  // === Engine Integration Tests ===

  @Test
  fun sandboxExecution_runsInCorrectSdk() {
    // Verify sandbox initialization with correct SDK
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @Test
  fun shadows_areConfigured() {
    // Verify shadows are properly configured
    val looper = Looper.getMainLooper()
    val shadowLooper = shadowOf(looper)
    assertThat(shadowLooper).isNotNull()

    var executed = false
    Handler(looper).post { executed = true }
    shadowLooper.idle()
    assertThat(executed).isTrue()
  }

  // === Configuration Tests ===

  @Nested
  @Config(sdk = [33])
  inner class NestedConfigTest {
    @Test
    fun nestedConfig_isApplied() {
      // Verify @Config inheritance in nested classes
      assertThat(Build.VERSION.SDK_INT).isEqualTo(33)
    }
  }

  // === Exception Handling Tests ===

  @Test
  fun exceptions_areCaught() {
    // Verify exception handling in Jupiter tests
    assertThrows<IllegalStateException> { throw IllegalStateException("test exception") }
  }

  // === Android Framework Integration Tests ===

  @Test
  fun sharedPreferences_works() {
    // Verify SharedPreferences integration
    val prefs =
      RuntimeEnvironment.getApplication().getSharedPreferences("test", Context.MODE_PRIVATE)
    prefs.edit().putString("key", "value").commit()

    val prefs2 =
      RuntimeEnvironment.getApplication().getSharedPreferences("test", Context.MODE_PRIVATE)
    assertThat(prefs2.getString("key", null)).isEqualTo("value")
  }

  @Test
  fun systemServices_available() {
    // Verify system services
    val context = RuntimeEnvironment.getApplication()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
    assertThat(activityManager).isInstanceOf(android.app.ActivityManager::class.java)
  }

  @Test
  fun broadcastReceiver_works() {
    // Verify BroadcastReceiver integration
    var received = false
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
          received = true
        }
      }

    val context = RuntimeEnvironment.getApplication()
    context.registerReceiver(receiver, IntentFilter("test.action"))
    context.sendBroadcast(Intent("test.action"))
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(received).isTrue()
    context.unregisterReceiver(receiver)
  }

  @Test
  fun looper_schedulesCallbacks() {
    // Verify Looper/Handler integration
    var executed = false
    Handler(Looper.getMainLooper()).postDelayed({ executed = true }, 100)

    assertThat(executed).isFalse()
    ShadowLooper.idleMainLooper(100, java.util.concurrent.TimeUnit.MILLISECONDS)
    assertThat(executed).isTrue()
  }

  // === Test Service ===

  class TestService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
  }
}

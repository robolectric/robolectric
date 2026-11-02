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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowView

@ExtendWith(RobolectricExtension::class)
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
      assertThat(activity.isVisible).isTrue()
    }
  }

  @Test
  fun clickOn_shouldThrowIfViewIsDisabled() {
    val view = View(context)
    view.isEnabled = false
    assertThrows<RuntimeException> { ShadowView.clickOn(view) }
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
    assertThrows<ActivityNotFoundException> {
      context.startActivity(Intent("i.dont.exist.activity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
  }

  // ===== SharedPreferences Tests =====

  @Test
  fun sharedPreferences_commit_shouldStoreAllTypes() {
    val prefs = context.getSharedPreferences("test_prefs", android.content.Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    val stringSet = setOf("str1", "str2", "str3")
    prefs
      .edit()
      .putBoolean("bool", true)
      .putFloat("float", 1.5f)
      .putInt("int", 42)
      .putLong("long", 123L)
      .putString("string", "hello")
      .putStringSet("stringSet", stringSet)
      .commit()

    val anotherPrefs =
      context.getSharedPreferences("test_prefs", android.content.Context.MODE_PRIVATE)
    assertThat(anotherPrefs.getBoolean("bool", false)).isTrue()
    assertThat(anotherPrefs.getFloat("float", 0f)).isEqualTo(1.5f)
    assertThat(anotherPrefs.getInt("int", 0)).isEqualTo(42)
    assertThat(anotherPrefs.getLong("long", 0L)).isEqualTo(123L)
    assertThat(anotherPrefs.getString("string", "")).isEqualTo("hello")
    assertThat(anotherPrefs.getStringSet("stringSet", null)).isEqualTo(stringSet)
  }

  @Test
  fun sharedPreferences_getAll_shouldReturnAllValues() {
    val prefs = context.getSharedPreferences("test_all", android.content.Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    prefs.edit().putInt("key1", 1).putString("key2", "value2").putBoolean("key3", true).commit()

    assertThat(prefs.all).hasSize(3)
    assertThat(prefs.all["key1"]).isEqualTo(1)
    assertThat(prefs.all["key2"]).isEqualTo("value2")
    assertThat(prefs.all["key3"]).isEqualTo(true)
  }

  @Test
  fun sharedPreferences_remove_shouldDeleteKey() {
    val prefs = context.getSharedPreferences("test_remove", android.content.Context.MODE_PRIVATE)
    prefs.edit().putString("deleteMe", "value").putString("keepMe", "keepValue").commit()

    prefs.edit().remove("deleteMe").commit()

    assertThat(prefs.contains("deleteMe")).isFalse()
    assertThat(prefs.getString("deleteMe", "default")).isEqualTo("default")
    assertThat(prefs.getString("keepMe", "")).isEqualTo("keepValue")
  }

  @Test
  fun sharedPreferences_clear_shouldRemoveAllValues() {
    val prefs = context.getSharedPreferences("test_clear", android.content.Context.MODE_PRIVATE)
    prefs.edit().putInt("key1", 1).putString("key2", "value").commit()

    prefs.edit().clear().commit()

    assertThat(prefs.all).isEmpty()
  }

  @Test
  fun sharedPreferences_putNull_shouldRemoveKey() {
    val prefs = context.getSharedPreferences("test_null", android.content.Context.MODE_PRIVATE)
    prefs.edit().putString("key", "value").commit()

    prefs.edit().putString("key", null).commit()

    assertThat(prefs.contains("key")).isFalse()
    assertThat(prefs.getString("key", null)).isNull()
  }

  @Test
  fun sharedPreferences_apply_shouldStoreValuesAsynchronously() {
    val prefs = context.getSharedPreferences("test_apply", android.content.Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    prefs.edit().putInt("async_key", 999).apply()

    Thread.sleep(100) // Give apply time to complete

    val anotherPrefs =
      context.getSharedPreferences("test_apply", android.content.Context.MODE_PRIVATE)
    assertThat(anotherPrefs.getInt("async_key", 0)).isEqualTo(999)
  }

  // ===== System Services Tests =====

  @Test
  fun systemServices_shouldProvideActivityManager() {
    val am = context.getSystemService(android.content.Context.ACTIVITY_SERVICE)
    assertThat(am).isInstanceOf(android.app.ActivityManager::class.java)
  }

  @Test
  fun systemServices_shouldProvideAlarmManager() {
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE)
    assertThat(alarmManager).isInstanceOf(android.app.AlarmManager::class.java)
  }

  @Test
  fun systemServices_shouldProvideLocationManager() {
    val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE)
    assertThat(lm).isInstanceOf(android.location.LocationManager::class.java)
  }

  @Test
  fun systemServices_shouldProvideConnectivityManager() {
    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
    assertThat(cm).isInstanceOf(android.net.ConnectivityManager::class.java)
  }

  // ===== Application Context Tests =====

  @Test
  fun application_shouldBeContext() {
    Robolectric.buildActivity(Activity::class.java).use { controller ->
      val app = controller.setup().get().application
      assertThat(app).isSameInstanceAs(context)
      assertThat(app.applicationContext).isSameInstanceAs(context)
    }
  }

  @Test
  fun application_packageName() {
    assertThat(context.packageName).isNotNull()
    assertThat(context.packageName).isNotEmpty()
  }

  // ===== BroadcastReceiver Tests =====

  @Test
  fun broadcastReceiver_shouldReceiveBroadcast() {
    var received = false
    val receiver =
      object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
          received = true
        }
      }

    val filter = android.content.IntentFilter("test.action.CUSTOM")
    context.registerReceiver(receiver, filter)

    val intent = Intent("test.action.CUSTOM")
    context.sendBroadcast(intent)

    ShadowLooper.idleMainLooper()

    assertThat(received).isTrue()
    context.unregisterReceiver(receiver)
  }

  @Test
  fun broadcastReceiver_shouldReceiveOrderedBroadcast() {
    var received = false
    val receiver =
      object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
          received = true
          resultCode = 42
        }
      }

    val filter = android.content.IntentFilter("test.action.ORDERED")
    context.registerReceiver(receiver, filter)

    context.sendOrderedBroadcast(Intent("test.action.ORDERED"), null)

    ShadowLooper.idleMainLooper()

    assertThat(received).isTrue()
    context.unregisterReceiver(receiver)
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

    val isVisible: Boolean
      get() = window.decorView.windowToken != null
  }
}

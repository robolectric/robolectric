package org.robolectric.integrationtests.junit.platform

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemProperties
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.view.View
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.testapp.TestActivity
import org.robolectric.testapp.TestService

@Config(sdk = [34])
class BasicRobolectricTest {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext()

  @Test
  fun activity_lifecycle_create() {
    Robolectric.buildActivity(TestActivity::class.java).use { controller ->
      val activity = controller.create().get()
      assertThat(activity).isNotNull()
    }
  }

  @Test
  fun activity_lifecycle_resume() {
    Robolectric.buildActivity(TestActivity::class.java).use { controller ->
      val activity = controller.setup().get()
      assertThat(activity).isNotNull()
    }
  }

  @Test
  fun activity_lifecycle_destroy() {
    val activity: TestActivity
    Robolectric.buildActivity(TestActivity::class.java).use { controller ->
      activity = controller.setup().get()
    }
    assertThat(activity.isDestroyed).isTrue()
  }

  @Test
  fun activity_title() {
    Robolectric.buildActivity(TestActivity::class.java).use { controller ->
      val activity = controller.setup().get()
      assertThat(activity.title.toString()).contains("TestActivity")
    }
  }

  @Test
  fun activity_intent_extras() {
    val intent = Intent(context, TestActivity::class.java).putExtra("key", "value")
    Robolectric.buildActivity(TestActivity::class.java, intent).use { controller ->
      val activity = controller.setup().get()
      assertThat(activity.intent.getStringExtra("key")).isEqualTo("value")
    }
  }

  @Test
  fun activity_findView() {
    Robolectric.buildActivity(TestActivity::class.java).use { controller ->
      val activity = controller.setup().get()
      val view = View(activity)
      view.id = 1234
      activity.setContentView(view)
      assertThat(activity.findViewById<View>(1234)).isEqualTo(view)
    }
  }

  @Test
  fun activity_finish() {
    Robolectric.buildActivity(TestActivity::class.java).use { controller ->
      val activity = controller.setup().get()
      activity.finish()
      assertThat(activity.isFinishing).isTrue()
    }
  }

  @Test
  fun activity_recreate() {
    Robolectric.buildActivity(TestActivity::class.java).use { controller ->
      val activity = controller.setup().get()
      controller.recreate()
      assertThat(controller.get()).isNotSameInstanceAs(activity)
    }
  }

  @Test
  fun service_create() {
    val controller = Robolectric.buildService(TestService::class.java)
    try {
      val service = controller.create().get()
      assertThat(service).isNotNull()
    } finally {
      controller.destroy()
    }
  }

  @Test
  fun service_bind() {
    val controller = Robolectric.buildService(TestService::class.java)
    try {
      val service = controller.create().bind().get()
      assertThat(service).isNotNull()
    } finally {
      controller.destroy()
    }
  }

  @Test
  fun service_destroy() {
    val controller = Robolectric.buildService(TestService::class.java)
    val service = controller.create().get()
    controller.destroy()
    assertThat(service).isNotNull()
  }

  @Test
  fun broadcastReceiver_receive() {
    val receiver =
      object : BroadcastReceiver() {
        var called = false

        override fun onReceive(context: Context?, intent: Intent?) {
          called = true
        }
      }
    context.registerReceiver(receiver, IntentFilter("test_action"), Context.RECEIVER_EXPORTED)
    context.sendBroadcast(Intent("test_action"))
    shadowOf(Looper.getMainLooper()).idle()
    assertThat(receiver.called).isTrue()
  }

  @Test
  fun broadcastReceiver_ordered() {
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          resultCode = 123
        }
      }
    context.sendOrderedBroadcast(Intent("test_action"), null, receiver, null, 0, null, null)
    shadowOf(Looper.getMainLooper()).idle()
    assertThat(receiver.resultCode).isEqualTo(123)
  }

  @Test
  fun contentProvider_query() {
    val provider =
      object : ContentProvider() {
        override fun onCreate(): Boolean = true

        override fun query(
          uri: Uri,
          p: Array<String>?,
          s: String?,
          sa: Array<String>?,
          so: String?,
        ): Cursor {
          return MatrixCursor(arrayOf("col")).apply { addRow(arrayOf("val")) }
        }

        override fun getType(uri: Uri): String? = null

        override fun insert(uri: Uri, v: ContentValues?): Uri? = null

        override fun delete(uri: Uri, s: String?, sa: Array<String>?): Int = 0

        override fun update(uri: Uri, v: ContentValues?, s: String?, sa: Array<String>?): Int = 0
      }
    Robolectric.setupContentProvider(provider.javaClass, "com.test.provider")
    val cursor =
      context.contentResolver.query(
        Uri.parse("content://com.test.provider"),
        null,
        null,
        null,
        null,
      )
    assertThat(cursor).isNotNull()
    cursor?.use {
      assertThat(it.moveToFirst()).isTrue()
      assertThat(it.getString(0)).isEqualTo("val")
    }
  }

  @Test
  fun resources_android_string() {
    val ok = context.getString(android.R.string.ok)
    assertThat(ok).isAnyOf("OK", "Ok")
  }

  @Test
  fun resources_configuration() {
    val config = context.resources.configuration
    assertThat(config).isNotNull()
  }

  @Test
  fun resources_displayMetrics() {
    val dm = context.resources.displayMetrics
    assertThat(dm.density).isGreaterThan(0f)
  }

  @Test
  fun graphics_bitmapCreate() {
    val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    assertThat(bitmap.width).isEqualTo(10)
    assertThat(bitmap.height).isEqualTo(10)
  }

  @Test
  fun graphics_rect() {
    val rect = Rect(0, 0, 50, 50)
    assertThat(rect.centerX()).isEqualTo(25)
    assertThat(rect.centerY()).isEqualTo(25)
  }

  @Test
  fun graphics_matrix() {
    val matrix = Matrix()
    assertThat(matrix.isIdentity).isTrue()
    matrix.postRotate(90f)
    assertThat(matrix.isIdentity).isFalse()
  }

  @Test
  fun graphics_colorParse() {
    val color = Color.parseColor("#FF0000")
    assertThat(color).isEqualTo(Color.RED)
  }

  @Test
  fun graphics_path() {
    val path = Path()
    assertThat(path.isEmpty).isTrue()
    path.lineTo(10f, 10f)
    assertThat(path.isEmpty).isFalse()
  }

  @Test
  fun systemService_powerManager() {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    assertThat(pm.isInteractive).isTrue()
  }

  @Test
  fun systemService_notificationManager() {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    assertThat(nm).isNotNull()
  }

  @Test
  fun systemService_connectivityManager() {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    assertThat(cm).isNotNull()
  }

  @Test
  fun systemService_alarmManager() {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    assertThat(am).isNotNull()
  }

  @Test
  fun systemService_telephonyManager() {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    assertThat(tm).isNotNull()
  }

  @Test
  fun systemService_vibrator() {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    assertThat(vibrator).isNotNull()
  }

  @Test
  fun systemService_wifiManager() {
    val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    assertThat(wifi).isNotNull()
  }

  @Test
  fun systemService_audioManager() {
    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    assertThat(audio).isNotNull()
  }

  @Test
  fun systemService_jobScheduler() {
    val js = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    assertThat(js).isNotNull()
  }

  @Test
  fun systemService_locationManager() {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    assertThat(lm).isNotNull()
  }

  @Test
  fun systemService_batteryManager() {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    assertThat(bm).isNotNull()
  }

  @Test
  fun systemService_windowManager() {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    assertThat(wm.defaultDisplay).isNotNull()
  }

  @Test
  fun environment_apiLevel() {
    assertThat(RuntimeEnvironment.getApiLevel()).isEqualTo(34)
  }

  @Test
  fun context_packageName() {
    assertThat(context.packageName).isNotNull()
  }

  @Test
  fun context_sharedPreferences() {
    val sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    sp.edit().putInt("val", 42).apply()
    assertThat(sp.getInt("val", 0)).isEqualTo(42)
  }

  @Test
  fun context_cacheDir() {
    assertThat(context.cacheDir).isNotNull()
  }

  @Test
  fun build_version() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @Test
  fun testLooper_MainLooper() {
    assertThat(Looper.getMainLooper()).isNotNull()
    assertThat(Thread.currentThread()).isEqualTo(Looper.getMainLooper().thread)
  }

  @Test
  fun systemProperties_getSet() {
    SystemProperties.set("debug.test", "1")
    assertThat(SystemProperties.get("debug.test")).isEqualTo("1")
  }

  @Test
  fun handler_post() {
    val handler = Handler(Looper.getMainLooper())
    var ran = false
    handler.post { ran = true }
    shadowOf(Looper.getMainLooper()).idle()
    assertThat(ran).isTrue()
  }

  @Test
  fun assets_manager() {
    assertThat(context.assets).isNotNull()
  }

  // ===== Additional SharedPreferences Tests =====

  @Test
  fun sharedPreferences_commit_shouldStoreAllTypes() {
    val prefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
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

    val anotherPrefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
    assertThat(anotherPrefs.getBoolean("bool", false)).isTrue()
    assertThat(anotherPrefs.getFloat("float", 0f)).isEqualTo(1.5f)
    assertThat(anotherPrefs.getInt("int", 0)).isEqualTo(42)
    assertThat(anotherPrefs.getLong("long", 0L)).isEqualTo(123L)
    assertThat(anotherPrefs.getString("string", "")).isEqualTo("hello")
    assertThat(anotherPrefs.getStringSet("stringSet", null)).isEqualTo(stringSet)
  }

  @Test
  fun sharedPreferences_getAll_shouldReturnAllValues() {
    val prefs = context.getSharedPreferences("test_all", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    prefs.edit().putInt("key1", 1).putString("key2", "value2").putBoolean("key3", true).commit()

    assertThat(prefs.all).hasSize(3)
    assertThat(prefs.all["key1"]).isEqualTo(1)
    assertThat(prefs.all["key2"]).isEqualTo("value2")
    assertThat(prefs.all["key3"]).isEqualTo(true)
  }

  @Test
  fun sharedPreferences_remove_shouldDeleteKey() {
    val prefs = context.getSharedPreferences("test_remove", Context.MODE_PRIVATE)
    prefs.edit().putString("deleteMe", "value").putString("keepMe", "keepValue").commit()

    prefs.edit().remove("deleteMe").commit()

    assertThat(prefs.contains("deleteMe")).isFalse()
    assertThat(prefs.getString("deleteMe", "default")).isEqualTo("default")
    assertThat(prefs.getString("keepMe", "")).isEqualTo("keepValue")
  }

  @Test
  fun sharedPreferences_clear_shouldRemoveAllValues() {
    val prefs = context.getSharedPreferences("test_clear", Context.MODE_PRIVATE)
    prefs.edit().putInt("key1", 1).putString("key2", "value").commit()

    prefs.edit().clear().commit()

    assertThat(prefs.all).isEmpty()
  }

  @Test
  fun sharedPreferences_putNull_shouldRemoveKey() {
    val prefs = context.getSharedPreferences("test_null", Context.MODE_PRIVATE)
    prefs.edit().putString("key", "value").commit()

    prefs.edit().putString("key", null).commit()

    assertThat(prefs.contains("key")).isFalse()
    assertThat(prefs.getString("key", null)).isNull()
  }

  @Test
  fun sharedPreferences_apply_shouldStoreValuesAsynchronously() {
    val prefs = context.getSharedPreferences("test_apply", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    prefs.edit().putInt("async_key", 999).apply()

    Thread.sleep(100) // Give apply time to complete

    val anotherPrefs = context.getSharedPreferences("test_apply", Context.MODE_PRIVATE)
    assertThat(anotherPrefs.getInt("async_key", 0)).isEqualTo(999)
  }

  // ===== System Services Tests =====

  @Test
  fun systemServices_shouldProvideActivityManager() {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE)
    assertThat(am).isInstanceOf(android.app.ActivityManager::class.java)
  }

  @Test
  fun systemServices_shouldProvideAlarmManager() {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
    assertThat(alarmManager).isInstanceOf(AlarmManager::class.java)
  }

  @Test
  fun systemServices_shouldProvideLocationManager() {
    val lm = context.getSystemService(Context.LOCATION_SERVICE)
    assertThat(lm).isInstanceOf(LocationManager::class.java)
  }

  @Test
  fun systemServices_shouldProvideConnectivityManager() {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
    assertThat(cm).isInstanceOf(ConnectivityManager::class.java)
  }

  // ===== Application Context Tests =====

  @Test
  fun application_shouldBeContext() {
    Robolectric.buildActivity(android.app.Activity::class.java).use { controller ->
      val app = controller.setup().get().application
      assertThat(app).isSameInstanceAs(ApplicationProvider.getApplicationContext<Context>())
      assertThat(app.applicationContext)
        .isSameInstanceAs(ApplicationProvider.getApplicationContext<Context>())
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
      object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          received = true
        }
      }

    val filter = IntentFilter("test.action.CUSTOM")
    context.registerReceiver(receiver, filter)

    val intent = Intent("test.action.CUSTOM")
    context.sendBroadcast(intent)

    shadowOf(Looper.getMainLooper()).idle()

    assertThat(received).isTrue()
    context.unregisterReceiver(receiver)
  }

  @Test
  fun broadcastReceiver_shouldReceiveOrderedBroadcast() {
    var received = false
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          received = true
          resultCode = 42
        }
      }

    val filter = IntentFilter("test.action.ORDERED")
    context.registerReceiver(receiver, filter)

    context.sendOrderedBroadcast(Intent("test.action.ORDERED"), null)

    shadowOf(Looper.getMainLooper()).idle()

    assertThat(received).isTrue()
    context.unregisterReceiver(receiver)
  }

  // ===== Service Binding Test =====

  @Test
  fun service_shouldBind() {
    val intent = Intent(context, TestService::class.java)
    var bound = false

    val connection =
      object : android.content.ServiceConnection {
        override fun onServiceConnected(
          name: android.content.ComponentName?,
          service: android.os.IBinder?,
        ) {
          bound = true
        }

        override fun onServiceDisconnected(name: android.content.ComponentName?) {
          bound = false
        }
      }

    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(bound).isTrue()
  }
}

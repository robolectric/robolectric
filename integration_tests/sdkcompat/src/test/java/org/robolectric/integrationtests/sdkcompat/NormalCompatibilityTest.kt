package org.robolectric.integrationtests.sdkcompat

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.speech.SpeechRecognizer
import android.view.PixelCopy
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.integrationtests.sdkcompat.MainActivity.CreationSource
import org.robolectric.testapp.TestActivity

@RunWith(RobolectricTestRunner::class)
class NormalCompatibilityTest {
  private val application = RuntimeEnvironment.getApplication()

  @Test
  fun `Environment SDK is 29`() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(Build.VERSION_CODES.Q)
  }

  @Test
  fun `Initialize LocationManager succeed`() {
    val locationManager = application.getSystemService(Context.LOCATION_SERVICE)
    assertThat(locationManager).isNotNull()
  }

  @Test
  fun `Initialize AppOpsManager succeed`() {
    val appOpsManager = application.getSystemService(Context.APP_OPS_SERVICE)
    assertThat(appOpsManager).isNotNull()
  }

  @Test
  fun `Initialize Activity and its shadow succeed`() {
    buildActivity(TestActivity::class.java).use { controller ->
      val activity = controller.setup().get()
      Shadows.shadowOf(activity)
    }
  }

  @Test
  fun `Initialize TelephonyManager succeed`() {
    val telephonyManager = application.getSystemService(Context.TELEPHONY_SERVICE)
    assertThat(telephonyManager).isNotNull()
  }

  @Test
  fun `Create speech recognizer succeed`() {
    assertThat(SpeechRecognizer.createSpeechRecognizer(application)).isNotNull()
  }

  @Test
  fun `Get default Vibrator succeed`() {
    assertThat(application.getSystemService(VIBRATOR_SERVICE) as Vibrator).isNotNull()
  }

  @Test
  fun `PixelCopy request`() {
    val testActivity = Robolectric.setupActivity(TestActivity::class.java)
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val listener = PixelCopy.OnPixelCopyFinishedListener {}
    val srcRect = Rect(0, 0, 100, 100)
    PixelCopy.request(
      testActivity.window,
      srcRect,
      bitmap,
      listener,
      Handler(Looper.getMainLooper()),
    )
  }

  @Test
  fun `MainActivity created correctly using AppComponentFactory`() {
    val activity = Robolectric.setupActivity(MainActivity::class.java)
    assertThat(activity.creationSource).isEqualTo(CreationSource.CUSTOM_CONSTRUCTOR)
  }

  @Test
  @Config(minSdk = 19, maxSdk = 27)
  fun `MainActivity created correctly using default constructor on api lower than 28`() {
    val activity = Robolectric.setupActivity(MainActivity::class.java)
    assertThat(activity.creationSource).isEqualTo(CreationSource.DEFAULT_CONSTRUCTOR)
  }
}

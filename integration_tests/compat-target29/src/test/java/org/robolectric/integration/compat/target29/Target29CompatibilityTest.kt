package org.robolectric.integration.compat.target29

import android.content.Context
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.testapp.TestActivity

@RunWith(RobolectricTestRunner::class)
class Target29CompatibilityTest {
  private val application = ApplicationProvider.getApplicationContext<Context>()

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
  fun `Initialize Activity succeed`() {
    ActivityScenario.launch(TestActivity::class.java).use { assertThat(true).isTrue() }
  }

  @Test
  fun `Environment SDK is 29`() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(Build.VERSION_CODES.Q)
  }
}

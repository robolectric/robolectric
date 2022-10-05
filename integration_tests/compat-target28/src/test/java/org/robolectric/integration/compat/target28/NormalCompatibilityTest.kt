package org.robolectric.integration.compat.target28

import android.content.Context
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.testapp.TestActivity

@RunWith(RobolectricTestRunner::class)
class NormalCompatibilityTest {
  private val application = RuntimeEnvironment.getApplication()

  @Test
  fun `Environment SDK is 28`() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(Build.VERSION_CODES.P)
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
  fun `Initialize Activity succeed`() {
    Robolectric.setupActivity(TestActivity::class.java)
  }
}

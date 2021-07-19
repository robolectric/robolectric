package org.robolectric.integration.compat.target29

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Target29CompatibilityTest {
    @Test
    fun `Initialize LocationManager succeed`() {
        val application = ApplicationProvider.getApplicationContext<Context>()
        val locationManager = application.getSystemService(Context.LOCATION_SERVICE)
        assertThat(locationManager).isNotNull()
    }
}

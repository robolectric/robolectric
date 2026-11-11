package org.robolectric.integrationtests.androidx

import android.app.Activity
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.WindowMetricsCalculator
import androidx.window.testing.layout.WindowMetricsCalculatorRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

/** Shows how to override the window metrics Jetpack WindowManager reports. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w673dp-h841dp-mdpi")
class WindowMetricsCalculatorRuleTest {
  @get:Rule val metricsRule = WindowMetricsCalculatorRule()

  @Test
  fun currentWindowBounds_canBeOverridden() {
    val activity = Robolectric.setupActivity(Activity::class.java)

    metricsRule.overrideCurrentWindowBounds(0, 0, 400, 600)

    assertThat(WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity).bounds)
      .isEqualTo(Rect(0, 0, 400, 600))
  }
}

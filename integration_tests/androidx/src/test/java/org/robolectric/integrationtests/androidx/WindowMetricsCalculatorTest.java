package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link WindowMetricsCalculator} */
@RunWith(AndroidJUnit4.class)
public class WindowMetricsCalculatorTest {
  @Test
  @Config(qualifiers = "w400dp-h600dp")
  public void computeCurrentWindowMetrics() {
    TestActivity activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    WindowMetrics windowMetrics =
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity);

    assertThat(windowMetrics.getBounds().width()).isEqualTo(400);
    assertThat(windowMetrics.getBounds().height()).isEqualTo(600);
  }
}

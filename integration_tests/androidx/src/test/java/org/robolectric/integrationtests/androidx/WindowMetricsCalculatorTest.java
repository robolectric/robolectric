package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.window.layout.WindowMetricsCalculator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link WindowMetricsCalculator} */
@RunWith(AndroidJUnit4.class)
public class WindowMetricsCalculatorTest {
  @Test
  @Config(qualifiers = "w400dp-h600dp")
  public void computeCurrentWindowMetrics() {
    try (ActivityController<TestActivity> activityController =
        Robolectric.buildActivity(TestActivity.class)) {
      TestActivity activity = activityController.setup().get();
      Rect bounds =
          WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity).getBounds();

      assertThat(bounds.width()).isEqualTo(400);
      assertThat(bounds.height()).isEqualTo(600);
    }
  }
}

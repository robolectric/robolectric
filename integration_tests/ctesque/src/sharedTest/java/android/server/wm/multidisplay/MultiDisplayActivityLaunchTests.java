/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.server.wm.multidisplay;

import static org.junit.Assert.assertEquals;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.server.wm.MultiDisplayTestBase;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.TestActivity;

/**
 * Tests activity launching behavior on multi-display environment.
 *
 * <p>Inspired from
 * cts/tests/framework/base/windowmanager/src/android/server/wm/multidisplay/MultiDisplayActivityLaunchTests.java.
 */
@Config(minSdk = Build.VERSION_CODES.O)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
public class MultiDisplayActivityLaunchTests extends MultiDisplayTestBase {
  /** Tests launching an activity on virtual display. */
  @Test
  public void testLaunchActivityOnSecondaryDisplay() {
    // Create new virtual display.
    final VirtualDisplaySession virtualDisplaySession = createManagedVirtualDisplaySession();
    final Display newDisplay = virtualDisplaySession.createDisplay();

    // Launch activity on new secondary display.
    final ActivityScenario<TestActivity> scenario =
        launchActivityOnDisplay(TestActivity.class, newDisplay.getDisplayId());
    waitAndAssertResumedActivityOnDisplay(
        scenario,
        newDisplay.getDisplayId(),
        "Activity launched on secondary display must be focused and on top");

    // Check that activity config corresponds to display config.
    final SizeInfo reportedSizes = getLastReportedSizesForActivity(scenario);
    assertEquals(
        "Activity launched on secondary display must have proper configuration",
        CUSTOM_DENSITY_DPI,
        reportedSizes.densityDpi);

    // CTS stops here. The activity must have the size of the display too, and fill it.
    final Size displaySize = virtualDisplaySession.getSize();
    assertEquals(displaySize.getWidth(), reportedSizes.displayWidth);
    assertEquals(displaySize.getHeight(), reportedSizes.displayHeight);
    assertEquals(displaySize.getWidth(), reportedSizes.metricsWidth);
    assertEquals(displaySize.getHeight(), reportedSizes.metricsHeight);
    // Releases differ in whether they round or truncate to dp.
    assertEquals(toDp(displaySize.getWidth()), reportedSizes.widthDp, 1 /* delta */);
    assertEquals(toDp(displaySize.getHeight()), reportedSizes.heightDp, 1 /* delta */);
    assertEquals(toDp(displaySize.getHeight()), reportedSizes.smallestWidthDp, 1 /* delta */);
    assertEquals(Configuration.ORIENTATION_LANDSCAPE, reportedSizes.orientation);
    waitForOrFail(
        "Activity launched on secondary display must fill it",
        () -> displaySize.equals(getWindowSize(scenario)));
  }

  /** Tests that the bounds of an activity launched on virtual display are those of the display. */
  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.R)
  public void testLaunchActivityOnSecondaryDisplay_windowMetrics() {
    final VirtualDisplaySession virtualDisplaySession = createManagedVirtualDisplaySession();
    final Display newDisplay = virtualDisplaySession.createDisplay();

    final ActivityScenario<TestActivity> scenario =
        launchActivityOnDisplay(TestActivity.class, newDisplay.getDisplayId());
    waitAndAssertResumedActivityOnDisplay(
        scenario,
        newDisplay.getDisplayId(),
        "Activity launched on secondary display must be focused and on top");

    final Size displaySize = virtualDisplaySession.getSize();
    final Rect displayBounds = new Rect(0, 0, displaySize.getWidth(), displaySize.getHeight());
    scenario.onActivity(
        activity -> {
          final WindowManager wm = activity.getWindowManager();
          assertEquals(displayBounds, wm.getCurrentWindowMetrics().getBounds());
          assertEquals(displayBounds, wm.getMaximumWindowMetrics().getBounds());
        });
  }

  /** Tests that an activity on virtual display stays there when it is relaunched. */
  @Test
  public void testRelaunchActivityOnSecondaryDisplay() {
    final Display newDisplay = createManagedVirtualDisplaySession().createDisplay();
    final ActivityScenario<TestActivity> scenario =
        launchActivityOnDisplay(TestActivity.class, newDisplay.getDisplayId());
    waitAndAssertResumedActivityOnDisplay(
        scenario,
        newDisplay.getDisplayId(),
        "Activity launched on secondary display must be focused and on top");
    final SizeInfo initialSizes = getLastReportedSizesForActivity(scenario);

    scenario.recreate();

    waitAndAssertResumedActivityOnDisplay(
        scenario,
        newDisplay.getDisplayId(),
        "Relaunched activity must be resumed on secondary display");
    assertEquals(
        "Sizes must not change after relaunch",
        initialSizes,
        getLastReportedSizesForActivity(scenario));
  }

  /** Converts pixels on a display that a {@link VirtualDisplaySession} created to dp. */
  private static float toDp(int pixels) {
    return pixels * 160f / CUSTOM_DENSITY_DPI;
  }

  private static Size getWindowSize(ActivityScenario<TestActivity> scenario) {
    final View decorView = getActivity(scenario).getWindow().getDecorView();
    return new Size(decorView.getWidth(), decorView.getHeight());
  }
}

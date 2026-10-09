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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.server.wm.MultiDisplayTestBase;
import android.view.Display;
import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.TestActivity;

/**
 * Tests each expected policy on multi-display environment.
 *
 * <p>Inspired from
 * cts/tests/framework/base/windowmanager/src/android/server/wm/multidisplay/MultiDisplayPolicyTests.java.
 */
@Config(minSdk = Build.VERSION_CODES.O)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
public class MultiDisplayPolicyTests extends MultiDisplayTestBase {
  /**
   * Tests that all activities that were on the private display are destroyed on display removal.
   */
  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
  public void testContentDestroyOnDisplayRemoved() {
    final ActivityScenario<TestActivity> testActivity;
    final ActivityScenario<ResizeableActivity> resizeableActivity;
    try (VirtualDisplaySession virtualDisplaySession = new VirtualDisplaySession()) {
      // Create new private virtual display.
      final Display newDisplay = virtualDisplaySession.createDisplay();

      // Launch activities on new secondary display.
      testActivity = launchActivityOnDisplay(TestActivity.class, newDisplay.getDisplayId());
      waitAndAssertResumedActivityOnDisplay(
          testActivity, newDisplay.getDisplayId(), "Launched activity must be resumed");

      resizeableActivity =
          launchActivityOnDisplay(ResizeableActivity.class, newDisplay.getDisplayId());
      waitAndAssertResumedActivityOnDisplay(
          resizeableActivity, newDisplay.getDisplayId(), "Launched activity must be resumed");

      // Destroy the display and check if activities are removed from system.
    }

    waitAndAssertActivityDestroyed(testActivity, "Activity from removed display must be destroyed");
    waitAndAssertActivityDestroyed(
        resizeableActivity, "Activity from removed display must be destroyed");
  }

  /** Tests that the update of display metrics updates all its content. */
  @Test
  public void testDisplayResize() {
    final VirtualDisplaySession virtualDisplaySession = createManagedVirtualDisplaySession();
    // Create new virtual display.
    final Display newDisplay = virtualDisplaySession.createDisplay();

    // Launch a resizeable activity on new secondary display.
    final ActivityScenario<ResizeableActivity> scenario =
        launchActivityOnDisplay(ResizeableActivity.class, newDisplay.getDisplayId());
    waitAndAssertResumedActivityOnDisplay(
        scenario, newDisplay.getDisplayId(), "Launched activity must be resumed");
    final ResizeableActivity activity = getActivity(scenario);

    // Grab reported sizes and compute new with slight size change.
    final SizeInfo initialSize = getLastReportedSizesForActivity(scenario);

    // Resize the display
    virtualDisplaySession.resizeDisplay();

    waitForOrFail(
        "the configuration change to happen and activity to be resumed",
        () -> getActivity(scenario).mConfigurationChangedCount == 1);

    // Check if activity in virtual display was resized properly.
    assertRelaunchOrConfigChanged(scenario, activity, 0 /* numRelaunch */, 1 /* numConfigChange */);

    final SizeInfo updatedSize = getLastReportedSizesForActivity(scenario);
    assertTrue(updatedSize.widthDp <= initialSize.widthDp);
    assertTrue(updatedSize.heightDp <= initialSize.heightDp);
    assertTrue(updatedSize.displayWidth == initialSize.displayWidth / 2);
    assertTrue(updatedSize.displayHeight == initialSize.displayHeight / 2);
    // CTS stops here. The window of the activity must be resized too.
    waitForOrFail(
        "the window of the activity to be resized",
        () -> {
          final Activity resized = getActivity(scenario);
          return resized.getWindow().getDecorView().getWidth() == updatedSize.displayWidth
              && resized.getWindow().getDecorView().getHeight() == updatedSize.displayHeight;
        });
  }

  /**
   * Tests that the update of display metrics relaunches an activity that doesn't handle the change.
   */
  @Test
  public void testDisplayResize_RelaunchActivity() {
    final VirtualDisplaySession virtualDisplaySession = createManagedVirtualDisplaySession();
    final Display newDisplay = virtualDisplaySession.createDisplay();

    final ActivityScenario<TestActivity> scenario =
        launchActivityOnDisplay(TestActivity.class, newDisplay.getDisplayId());
    waitAndAssertResumedActivityOnDisplay(
        scenario, newDisplay.getDisplayId(), "Launched activity must be resumed");
    final TestActivity activity = getActivity(scenario);
    final SizeInfo initialSize = getLastReportedSizesForActivity(scenario);

    // Change the size and the density of the display.
    virtualDisplaySession.changeDisplayMetrics(1.2 /* sizeRatio */, 1.1 /* densityRatio */);

    waitForOrFail(
        "the activity to be relaunched and resumed",
        () ->
            getLastReportedSizesForActivity(scenario).densityDpi
                == virtualDisplaySession.getDensityDpi());
    waitAndAssertResumedActivityOnDisplay(
        scenario, newDisplay.getDisplayId(), "Relaunched activity must be resumed");
    assertNotSame(activity, getActivity(scenario));

    final SizeInfo updatedSize = getLastReportedSizesForActivity(scenario);
    assertEquals(virtualDisplaySession.getSize().getWidth(), updatedSize.displayWidth);
    assertEquals(virtualDisplaySession.getSize().getHeight(), updatedSize.displayHeight);
    assertTrue(updatedSize.displayWidth > initialSize.displayWidth);
    assertTrue(updatedSize.displayHeight > initialSize.displayHeight);
    waitForOrFail(
        "the window of the relaunched activity to fill the display",
        () -> {
          final Activity relaunched = getActivity(scenario);
          return relaunched.getWindow().getDecorView().getWidth() == updatedSize.displayWidth
              && relaunched.getWindow().getDecorView().getHeight() == updatedSize.displayHeight;
        });
  }

  /** Asserts how often an activity was relaunched and told that its configuration changed. */
  private static void assertRelaunchOrConfigChanged(
      ActivityScenario<ResizeableActivity> scenario,
      ResizeableActivity activity,
      int numRelaunch,
      int numConfigChange) {
    final ResizeableActivity currentActivity = getActivity(scenario);
    if (numRelaunch == 0) {
      assertSame("Activity must not be relaunched", activity, currentActivity);
    } else {
      assertNotSame("Activity must be relaunched", activity, currentActivity);
    }
    assertEquals(
        "Activity must receive configuration changes",
        numConfigChange,
        currentActivity.mConfigurationChangedCount);
  }

  /**
   * An activity that handles the configuration changes of a display resize, as CTS's
   * ResizeableActivity.
   */
  public static class ResizeableActivity extends Activity {
    volatile int mConfigurationChangedCount;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      mConfigurationChangedCount++;
    }
  }
}

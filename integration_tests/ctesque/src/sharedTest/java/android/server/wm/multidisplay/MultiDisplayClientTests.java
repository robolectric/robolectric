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

import android.os.Build;
import android.server.wm.MultiDisplayTestBase;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.TestActivity;

/**
 * Tests the display that the views of an activity are on in a multi-display environment.
 *
 * <p>Inspired from
 * cts/tests/framework/base/windowmanager/src/android/server/wm/multidisplay/MultiDisplayClientTests.java.
 */
@Config(minSdk = Build.VERSION_CODES.O)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
public class MultiDisplayClientTests extends MultiDisplayTestBase {
  @Test
  public void testViewGetDisplayOnPrimaryDisplay() {
    testViewGetDisplay(true /* isPrimary */);
  }

  @Test
  public void testViewGetDisplayOnSecondaryDisplay() {
    testViewGetDisplay(false /* isPrimary */);
  }

  private void testViewGetDisplay(boolean isPrimary) {
    final Display newDisplay = createManagedVirtualDisplaySession().createDisplay();
    final int displayId = isPrimary ? Display.DEFAULT_DISPLAY : newDisplay.getDisplayId();

    final ActivityScenario<TestActivity> scenario =
        launchActivityOnDisplay(TestActivity.class, displayId);
    waitAndAssertResumedActivityOnDisplay(
        scenario, displayId, "Activity launched on display:" + displayId + " must be focused");

    final int[] resultDisplayId = {Display.INVALID_DISPLAY};
    final View[] addedView = new View[1];
    scenario.onActivity(
        activity -> {
          // Test View#getdisplay() from activity
          final View view = activity.getWindow().getDecorView();
          assertEquals(
              "View#getDisplay() must match.", displayId, view.getDisplay().getDisplayId());

          // Test View#getdisplay() from WM#addView()
          final WindowManager wm = activity.getWindowManager();
          addedView[0] = new View(activity);
          // Get display ID from callback in case the added view has not be attached.
          addedView[0].addOnAttachStateChangeListener(
              new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                  resultDisplayId[0] = view.getDisplay().getDisplayId();
                }

                @Override
                public void onViewDetachedFromWindow(View view) {}
              });
          wm.addView(addedView[0], new WindowManager.LayoutParams());
        });
    try {
      waitForOrFail(
          "Display from added view must match. Should be display:" + displayId,
          () -> displayId == resultDisplayId[0]);
    } finally {
      scenario.onActivity(
          activity -> activity.getWindowManager().removeViewImmediate(addedView[0]));
    }
  }
}

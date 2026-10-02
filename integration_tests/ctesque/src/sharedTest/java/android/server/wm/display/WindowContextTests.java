/*
 * Copyright (C) 2020 The Android Open Source Project
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
package android.server.wm.display;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.server.wm.MultiDisplayTestBase;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import android.view.WindowManager;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.R;

/**
 * Tests that verify the behavior of window context
 *
 * <p>Inspired from
 * cts/tests/framework/base/windowmanager/src/android/server/wm/display/WindowContextTests.java.
 */
@Config(minSdk = Build.VERSION_CODES.S)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
public class WindowContextTests extends MultiDisplayTestBase {
  /**
   * Verifies that a window context on a secondary display has the bounds, the configuration and the
   * resources of that display.
   */
  @Test
  public void testWindowContextOnSecondaryDisplay() {
    final VirtualDisplaySession virtualDisplaySession = createManagedVirtualDisplaySession();
    final Display display = virtualDisplaySession.createDisplay();
    final Context windowContext = createWindowContext(display.getDisplayId());

    final Rect bounds =
        windowContext.getSystemService(WindowManager.class).getCurrentWindowMetrics().getBounds();
    assertBoundsEquals(virtualDisplaySession.getSize(), bounds);

    final Configuration config = windowContext.getResources().getConfiguration();
    assertEquals(CUSTOM_DENSITY_DPI, config.densityDpi);
    assertEquals(Configuration.ORIENTATION_LANDSCAPE, config.orientation);

    final DisplayMetrics metrics = windowContext.getResources().getDisplayMetrics();
    assertEquals(virtualDisplaySession.getSize().getWidth(), metrics.widthPixels);
    assertEquals(virtualDisplaySession.getSize().getHeight(), metrics.heightPixels);
    assertEquals(CUSTOM_DENSITY_DPI, metrics.densityDpi);
    // 8dp at the density of the display.
    assertEquals(
        (int) (8 * CUSTOM_DENSITY_DPI / 160f + 0.5f),
        windowContext.getResources().getDimensionPixelSize(R.dimen.test_dp_dimen));
  }

  /** Verifies that a window context on the default display has the bounds of that display. */
  @Test
  public void testWindowContextOnDefaultDisplay() {
    // A secondary display must not change what a window context on the default display has.
    createManagedVirtualDisplaySession().createDisplay();
    final Point displaySize = new Point();
    mDm.getDisplay(Display.DEFAULT_DISPLAY).getRealSize(displaySize);

    final Context windowContext = createWindowContext(Display.DEFAULT_DISPLAY);

    final Rect bounds =
        windowContext.getSystemService(WindowManager.class).getCurrentWindowMetrics().getBounds();
    assertBoundsEquals(new Size(displaySize.x, displaySize.y), bounds);
  }

  private void assertBoundsEquals(Size expectedSize, Rect bounds) {
    assertEquals(expectedSize.getWidth(), bounds.width());
    assertEquals(expectedSize.getHeight(), bounds.height());
  }
}

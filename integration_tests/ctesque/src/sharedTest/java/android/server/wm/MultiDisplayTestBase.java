/*
 * Copyright (C) 2023 The Android Open Source Project
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
package android.server.wm;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Base class for the multi-display tests.
 *
 * <p>Inspired from
 * cts/tests/framework/base/windowmanager/util/src/android/server/wm/MultiDisplayTestBase.java and
 * the classes it builds on: ActivityManagerTestBase.java, VirtualDisplayHelper.java and
 * CommandSession.java.
 *
 * <p>CTS creates its displays and activities in helper apps, and reads the state of the window
 * manager from dumpsys. This class only uses framework APIs, so that the tests also run on
 * Robolectric.
 */
@RunWith(AndroidJUnit4.class)
public abstract class MultiDisplayTestBase {
  public static final int CUSTOM_DENSITY_DPI = 222;

  private static final String VIRTUAL_DISPLAY_NAME = "CtsVirtualDisplay";
  private static final int WIDTH = 800;
  private static final int HEIGHT = 480;
  private static final long TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);
  private static final long RETRY_INTERVAL_MS = 50;

  protected Instrumentation mInstrumentation;
  protected Context mContext;
  protected DisplayManager mDm;

  /** The sessions and the activities to close after a test, as CTS's ObjectTracker does. */
  private final List<AutoCloseable> mManagedObjects = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    mInstrumentation = InstrumentationRegistry.getInstrumentation();
    mContext = ApplicationProvider.getApplicationContext();
    mDm = mContext.getSystemService(DisplayManager.class);
  }

  @After
  public void closeManagedObjects() throws Exception {
    for (int i = mManagedObjects.size() - 1; i >= 0; i--) {
      mManagedObjects.get(i).close();
    }
    mManagedObjects.clear();
  }

  /** Creates a session whose virtual display is released after the test. */
  protected VirtualDisplaySession createManagedVirtualDisplaySession() {
    final VirtualDisplaySession session = new VirtualDisplaySession();
    mManagedObjects.add(session);
    return session;
  }

  /**
   * Launches an activity on a display. It is finished after the test.
   *
   * <p>Since Q, only the shell and the system may launch an activity on the virtual display of an
   * app, so it is launched with the shell's permissions, as CTS does.
   */
  protected <T extends Activity> ActivityScenario<T> launchActivityOnDisplay(
      Class<T> activityClass, int displayId) {
    final Intent intent = new Intent(mContext, activityClass);
    final ActivityOptions launchOptions = ActivityOptions.makeBasic();
    launchOptions.setLaunchDisplayId(displayId);
    final Bundle bundle = launchOptions.toBundle();
    final ActivityScenario<T> scenario;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      scenario = ActivityScenario.launch(intent, bundle);
    } else {
      final UiAutomation uiAutomation = mInstrumentation.getUiAutomation();
      uiAutomation.adoptShellPermissionIdentity();
      try {
        scenario = ActivityScenario.launch(intent, bundle);
      } finally {
        uiAutomation.dropShellPermissionIdentity();
      }
    }
    mManagedObjects.add(scenario);
    return scenario;
  }

  protected static void waitAndAssertResumedActivityOnDisplay(
      ActivityScenario<? extends Activity> scenario, int displayId, String message) {
    waitForOrFail(
        message,
        () ->
            scenario.getState() == Lifecycle.State.RESUMED
                && getActivity(scenario).getWindowManager().getDefaultDisplay().getDisplayId()
                    == displayId);
  }

  protected static void waitAndAssertActivityDestroyed(
      ActivityScenario<? extends Activity> scenario, String message) {
    waitForOrFail(message, () -> scenario.getState() == Lifecycle.State.DESTROYED);
  }

  protected static <T extends Activity> T getActivity(ActivityScenario<T> scenario) {
    final AtomicReference<T> activity = new AtomicReference<>();
    scenario.onActivity(activity::set);
    return activity.get();
  }

  /**
   * Returns the sizes of an activity. CTS's test activities report them to the test in their
   * callbacks.
   */
  protected static SizeInfo getLastReportedSizesForActivity(
      ActivityScenario<? extends Activity> scenario) {
    final AtomicReference<SizeInfo> sizeInfo = new AtomicReference<>();
    scenario.onActivity(
        activity ->
            sizeInfo.set(
                new SizeInfo(
                    activity.getWindowManager().getDefaultDisplay(),
                    activity.getResources().getDisplayMetrics(),
                    activity.getResources().getConfiguration())));
    return sizeInfo.get();
  }

  protected Context createWindowContext(int displayId) {
    final Display display = mDm.getDisplay(displayId);
    return mContext
        .createDisplayContext(display)
        .createWindowContext(TYPE_APPLICATION_OVERLAY, null /* options */);
  }

  /**
   * Waits for a condition, which changes of the window manager meet asynchronously on a device, and
   * fails if it isn't met in time.
   */
  protected static void waitForOrFail(String message, BooleanSupplier condition) {
    final long deadline = SystemClock.uptimeMillis() + TIMEOUT_MS;
    while (!condition.getAsBoolean()) {
      if (SystemClock.uptimeMillis() >= deadline) {
        fail(message);
      }
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      SystemClock.sleep(RETRY_INTERVAL_MS);
    }
  }

  /** The sizes of an activity, from CommandSession.SizeInfo. */
  public static class SizeInfo {
    public int widthDp;
    public int heightDp;
    public int displayWidth;
    public int displayHeight;
    public int metricsWidth;
    public int metricsHeight;
    public int smallestWidthDp;
    public int densityDpi;
    public int orientation;

    public SizeInfo(Display display, DisplayMetrics metrics, Configuration config) {
      if (display != null) {
        final Point displaySize = new Point();
        display.getSize(displaySize);
        displayWidth = displaySize.x;
        displayHeight = displaySize.y;
      }

      widthDp = config.screenWidthDp;
      heightDp = config.screenHeightDp;
      metricsWidth = metrics.widthPixels;
      metricsHeight = metrics.heightPixels;
      smallestWidthDp = config.smallestScreenWidthDp;
      densityDpi = config.densityDpi;
      orientation = config.orientation;
    }

    @Override
    public String toString() {
      return "SizeInfo: {widthDp="
          + widthDp
          + " heightDp="
          + heightDp
          + " displayWidth="
          + displayWidth
          + " displayHeight="
          + displayHeight
          + " metricsWidth="
          + metricsWidth
          + " metricsHeight="
          + metricsHeight
          + " smallestWidthDp="
          + smallestWidthDp
          + " densityDpi="
          + densityDpi
          + " orientation="
          + orientation
          + "}";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof SizeInfo)) {
        return false;
      }
      final SizeInfo that = (SizeInfo) obj;
      return widthDp == that.widthDp
          && heightDp == that.heightDp
          && displayWidth == that.displayWidth
          && displayHeight == that.displayHeight
          && metricsWidth == that.metricsWidth
          && metricsHeight == that.metricsHeight
          && smallestWidthDp == that.smallestWidthDp
          && densityDpi == that.densityDpi
          && orientation == that.orientation;
    }

    @Override
    public int hashCode() {
      int result = 0;
      result = 31 * result + widthDp;
      result = 31 * result + heightDp;
      result = 31 * result + displayWidth;
      result = 31 * result + displayHeight;
      result = 31 * result + metricsWidth;
      result = 31 * result + metricsHeight;
      result = 31 * result + smallestWidthDp;
      result = 31 * result + densityDpi;
      result = 31 * result + orientation;
      return result;
    }
  }

  /**
   * Creates a private virtual display of an app, as VirtualDisplayHelper does, and releases it when
   * closed.
   */
  public class VirtualDisplaySession implements AutoCloseable {
    private int mDensityDpi = CUSTOM_DENSITY_DPI;
    private boolean mPresentationDisplay;
    private Size mSize = new Size(WIDTH, HEIGHT);
    private ImageReader mReader;
    private VirtualDisplay mVirtualDisplay;

    public VirtualDisplaySession setPresentationDisplay(boolean presentationDisplay) {
      mPresentationDisplay = presentationDisplay;
      return this;
    }

    public Display createDisplay() {
      mReader =
          ImageReader.newInstance(
              mSize.getWidth(), mSize.getHeight(), PixelFormat.RGBA_8888, 2 /* maxImages */);
      final int flags = mPresentationDisplay ? DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION : 0;
      mVirtualDisplay =
          mDm.createVirtualDisplay(
              VIRTUAL_DISPLAY_NAME,
              mSize.getWidth(),
              mSize.getHeight(),
              mDensityDpi,
              mReader.getSurface(),
              flags);
      return mVirtualDisplay.getDisplay();
    }

    /** Returns the size of the virtual display now. */
    public Size getSize() {
      return mSize;
    }

    /** Returns the density of the virtual display now. */
    public int getDensityDpi() {
      return mDensityDpi;
    }

    /** Resizes the virtual display to half of its size, as VirtualDisplayActivity does. */
    public void resizeDisplay() {
      changeDisplayMetrics(0.5 /* sizeRatio */, 1 /* densityRatio */);
    }

    /** Changes the size and the density of the virtual display, as DisplayMetricsSession does. */
    public void changeDisplayMetrics(double sizeRatio, double densityRatio) {
      mSize = new Size((int) (mSize.getWidth() * sizeRatio), (int) (mSize.getHeight() * sizeRatio));
      mDensityDpi = (int) (mDensityDpi * densityRatio);
      mVirtualDisplay.resize(mSize.getWidth(), mSize.getHeight(), mDensityDpi);
    }

    @Override
    public void close() {
      if (mVirtualDisplay != null) {
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        mReader.close();
      }
    }
  }
}

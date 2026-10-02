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

import static android.view.WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.Presentation;
import android.os.Build;
import android.server.wm.MultiDisplayTestBase;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.SdkSuppress;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.TestActivity;

/**
 * Tests the presentations on a virtual display. Since R, a presentation on a private display is a
 * private presentation without the app asking for it.
 *
 * <p>Inspired from
 * cts/tests/framework/base/windowmanager/src/android/server/wm/display/PresentationTest.java.
 */
@Config(minSdk = Build.VERSION_CODES.R)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.R)
public class PresentationTest extends MultiDisplayTestBase {
  private VirtualDisplaySession mVirtualDisplaySession;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    mVirtualDisplaySession = createManagedVirtualDisplaySession();
  }

  /** Asserts that a private presentation is created on a private presentation display. */
  @Test
  public void testPrivatePresentationCreatedOnPrivatePresentationDisplay() {
    final Display privatePresentationDisplay = createPrivatePresentationDisplay();
    final ActivityScenario<TestActivity> scenario = launchPresentationActivity();
    final Presentation presentation =
        showPresentation(scenario, privatePresentationDisplay.getDisplayId());
    waitAndAssertPresentationOnDisplayAndMatchesDisplayMetrics(
        scenario, presentation, privatePresentationDisplay.getDisplayId());
  }

  /** Asserts that a presentation isn't dismissed with display resize. */
  @Test
  @Config(minSdk = Build.VERSION_CODES.S)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
  public void testPresentationNotDismissAfterResizeDisplay() {
    final Display display = createPrivatePresentationDisplay();
    final ActivityScenario<TestActivity> scenario = launchPresentationActivity();
    final Presentation presentation = showPresentation(scenario, display.getDisplayId());
    waitAndAssertPresentationOnDisplayAndMatchesDisplayMetrics(
        scenario, presentation, display.getDisplayId());

    mVirtualDisplaySession.resizeDisplay();

    waitAndAssertPresentationOnDisplayAndMatchesDisplayMetrics(
        scenario, presentation, display.getDisplayId());
  }

  /** Asserts that a presentation is dismissed when its display is removed. */
  @Test
  public void testPresentationDismissAfterRemoveDisplay() {
    final Display display = createPrivatePresentationDisplay();
    final ActivityScenario<TestActivity> scenario = launchPresentationActivity();
    final Presentation presentation = showPresentation(scenario, display.getDisplayId());
    waitAndAssertPresentationOnDisplayAndMatchesDisplayMetrics(
        scenario, presentation, display.getDisplayId());

    mVirtualDisplaySession.close();

    waitForOrFail(
        "Presentation must dismiss when its display is removed",
        () -> !isShowing(scenario, presentation));
  }

  private void waitAndAssertPresentationOnDisplayAndMatchesDisplayMetrics(
      ActivityScenario<TestActivity> scenario, Presentation presentation, int displayId) {
    waitForOrFail(
        "Presentation that matches display metrics didn't show up",
        () -> {
          final AtomicReference<Boolean> matches = new AtomicReference<>();
          scenario.onActivity(
              activity -> {
                final View decorView = presentation.getWindow().getDecorView();
                final Size size = mVirtualDisplaySession.getSize();
                matches.set(
                    presentation.isShowing()
                        && presentation.getWindow().getAttributes().type
                            == TYPE_PRIVATE_PRESENTATION
                        && decorView.getDisplay() != null
                        && decorView.getDisplay().getDisplayId() == displayId
                        && decorView.getWidth() == size.getWidth()
                        && decorView.getHeight() == size.getHeight());
              });
          return matches.get();
        });
    assertThat(presentation.getDisplay().getDisplayId()).isEqualTo(displayId);
    assertThat(presentation.getContext().getResources().getConfiguration().densityDpi)
        .isEqualTo(mVirtualDisplaySession.getDensityDpi());
  }

  private Display createPrivatePresentationDisplay() {
    final Display display = mVirtualDisplaySession.setPresentationDisplay(true).createDisplay();
    assertThat((display.getFlags() & Display.FLAG_PRESENTATION) == Display.FLAG_PRESENTATION)
        .isTrue();
    assertThat((display.getFlags() & Display.FLAG_PRIVATE) == Display.FLAG_PRIVATE).isTrue();
    return display;
  }

  /** Launches the activity that shows the presentations, on the default display. */
  private ActivityScenario<TestActivity> launchPresentationActivity() {
    final ActivityScenario<TestActivity> scenario =
        launchActivityOnDisplay(TestActivity.class, Display.DEFAULT_DISPLAY);
    waitAndAssertResumedActivityOnDisplay(
        scenario, Display.DEFAULT_DISPLAY, "Launched activity must be on top");
    return scenario;
  }

  /** Shows a presentation as CTS's PresentationActivity does. */
  private Presentation showPresentation(
      ActivityScenario<? extends Activity> scenario, int displayId) {
    final AtomicReference<Presentation> presentation = new AtomicReference<>();
    scenario.onActivity(
        activity -> {
          final Display presentationDisplay = mDm.getDisplay(displayId);
          final TextView view = new TextView(activity);
          view.setText("I'm a presentation");
          view.setGravity(Gravity.CENTER);
          presentation.set(new Presentation(activity, presentationDisplay));
          presentation.get().setContentView(view);
          presentation.get().setTitle(getClass().getSimpleName());
          presentation.get().show();
        });
    return presentation.get();
  }

  private static boolean isShowing(
      ActivityScenario<? extends Activity> scenario, Presentation presentation) {
    final AtomicReference<Boolean> showing = new AtomicReference<>();
    scenario.onActivity(activity -> showing.set(presentation.isShowing()));
    return showing.get();
  }
}

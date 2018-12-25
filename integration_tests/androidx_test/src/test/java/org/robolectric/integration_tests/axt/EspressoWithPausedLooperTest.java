package org.robolectric.integration_tests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.integration.axt.R;
import org.robolectric.shadows.ShadowLooper;

/** Verify Espresso usage with paused looper */
@RunWith(AndroidJUnit4.class)
public final class EspressoWithPausedLooperTest {

  @Before
  public void setUp() {
    ShadowLooper.pauseMainLooper();
    ActivityScenario.launch(EspressoActivity.class);
  }

  @Test
  public void launchActivity() {}

}

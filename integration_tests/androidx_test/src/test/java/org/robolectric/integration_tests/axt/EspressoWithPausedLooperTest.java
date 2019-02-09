package org.robolectric.integration_tests.axt;

import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowBaseLooper;
import org.robolectric.shadows.ShadowLooper;

/** Verify Espresso usage with paused looper */
@RunWith(AndroidJUnit4.class)
public final class EspressoWithPausedLooperTest {

  @Before
  public void setUp() {
    if (!ShadowBaseLooper.useRealisticLooper()) {
      ShadowLooper.pauseMainLooper();
    }
    ActivityScenario.launch(EspressoActivity.class);
  }

  @Test
  public void launchActivity() {}

}

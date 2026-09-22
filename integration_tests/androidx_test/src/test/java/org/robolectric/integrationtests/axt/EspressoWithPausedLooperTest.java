package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.integration.axt.R;

/** Verify Espresso usage with paused looper */
@RunWith(AndroidJUnit4.class)
public final class EspressoWithPausedLooperTest {

  @Before
  public void setUp() {
    shadowMainLooper().pause();
    ActivityScenario.launch(EspressoActivity.class);
  }

  @Test
  public void launchActivity() {}

  @Test
  public void onIdle_doesnt_block() {
    Espresso.onIdle();
  }

  /** Perform the equivalent of launchActivityAndFindView_ById except using espresso APIs */
  @Test
  public void launchActivityAndFindView_espresso() {
    onView(withId(R.id.edit_text)).check(matches(isCompletelyDisplayed()));
  }
}

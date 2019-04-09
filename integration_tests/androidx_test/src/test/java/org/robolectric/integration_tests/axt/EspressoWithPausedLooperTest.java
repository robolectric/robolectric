package org.robolectric.integration_tests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;
import static org.robolectric.annotation.TextLayoutMode.Mode.REALISTIC;
import static org.robolectric.shadows.ShadowBaseLooper.shadowMainLooper;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.integration.axt.R;

/** Verify Espresso usage with paused looper */
@RunWith(AndroidJUnit4.class)
@TextLayoutMode(REALISTIC)
@LooperMode(PAUSED)
@Config(sdk = 28)// DO NOT SUBMIT
public final class EspressoWithPausedLooperTest {

  @Before
  public void setUp() {
    shadowMainLooper().pause();

  }

  @Test
  public void launchActivity() {
    ActivityScenario.launch(EspressoActivity.class);
  }

  @Test
  public void onIdle_doesnt_block() throws Exception {
    Espresso.onIdle();
  }

  /** Perform the equivalent of launchActivityAndFindView_ById except using espresso APIs */
  @Test
  public void launchActivityAndFindView_espresso() throws Exception {
    ActivityScenario.launch(EspressoActivity.class);

    onView(withId(R.id.edit_text)).check(matches(isCompletelyDisplayed()));
  }

  @Test
  public void fragmentScenario() throws Exception {
    FragmentScenario.launchInContainer(EspressoFragment.class);

    assertThat(shadowMainLooper().isIdle()).isTrue();
  }
}

package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test file for Espresso related to label. */
@RunWith(AndroidJUnit4.class)
public class EspressoLabelTest {
  @Rule
  public ActivityScenarioRule<EspressoActivity> activityRule =
      new ActivityScenarioRule<>(EspressoActivity.class);

  @Test
  public void customActivityLabel() {
    onView(withText("Activity Label")).check(matches(isCompletelyDisplayed()));
  }
}

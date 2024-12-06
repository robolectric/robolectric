package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.integration.axt.R;

/** Test file for Espresso with scrolling logic. */
@RunWith(AndroidJUnit4.class)
public class EspressoScrollingTest {
  @Rule
  public ActivityScenarioRule<EspressoScrollingActivity> activityRule =
      new ActivityScenarioRule<>(EspressoScrollingActivity.class);

  @Test
  public void clickButton_after_swipeUp() {
    onView(withId(R.id.scroll_view)).perform(swipeUp());
    onView(withId(R.id.button)).perform(click());
    activityRule.getScenario().onActivity(action -> assertThat(action.buttonClicked).isTrue());
  }
}

package org.robolectric.integration_tests.atsl;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.os.Bundle;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.integration.atsl.R;

/** Simple tests to verify espresso APIs can be used on both Robolectric and device. */
@RunWith(AndroidJUnit4.class)
public final class EspressoTest {

  @Rule
  public ActivityTestRule<EspressoActivity> activityRule =
      new ActivityTestRule<>(EspressoActivity.class, false, true);

  @Test
  public void onIdle_doesnt_block() throws Exception {
    Espresso.onIdle();
  }

  @Test
  public void launchActivityAndFindView_ById() throws Exception {
    EspressoActivity activity = activityRule.getActivity();

    TextView textView = (TextView) activity.findViewById(R.id.text);
    assertThat(textView).isNotNull();
    assertThat(textView.isEnabled()).isTrue();
  }

  /**
   * Perform the equivalent of launchActivityAndFindView_ById except using espresso APIs
   */
  @Test
  public void launchActivityAndFindView_espresso() throws Exception {
    onView(withId(R.id.text)).check(matches(isEnabled()));
  }

  /** Perform the 'traditional' mechanism of clicking a button in Robolectric using findViewById */
  @Test
  @UiThreadTest
  public void buttonClick() throws Exception {
    EspressoActivity activity = activityRule.getActivity();
    Button button = activity.findViewById(R.id.button);
    button.performClick();
    assertThat(activity.buttonClicked).isTrue();
  }

  /** Perform the equivalent of click except using espresso APIs */
  @Test
  public void buttonClick_espresso() throws Exception {
    EspressoActivity activity = activityRule.getActivity();
    onView(withId(R.id.button)).perform(click());
    assertThat(activity.buttonClicked).isTrue();
  }
}

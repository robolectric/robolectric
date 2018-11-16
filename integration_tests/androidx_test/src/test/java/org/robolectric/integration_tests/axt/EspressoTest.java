package org.robolectric.integration_tests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;

import android.widget.Button;
import android.widget.EditText;
import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.integration.axt.R;

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

    EditText editText = activity.findViewById(R.id.text);

    assertThat(editText).isNotNull();
    assertThat(editText.isEnabled()).isTrue();
  }

  /** Perform the equivalent of launchActivityAndFindView_ById except using espresso APIs */
  @Test
  public void launchActivityAndFindView_espresso() throws Exception {
    onView(withId(R.id.text)).check(matches(isCompletelyDisplayed()));
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

  /** Perform the 'traditional' mechanism of setting contents of a text view using findViewById */
  @Test
  @UiThreadTest
  public void typeText() throws Exception {
    EspressoActivity activity = activityRule.getActivity();
    EditText editText = activity.findViewById(R.id.text);
    editText.setText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org");

    assertThat(editText.getText().toString())
        .isEqualTo("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org");
  }

  /** Perform the equivalent of setText except using espresso APIs */
  @Test
  public void typeText_espresso() throws Exception {
    onView(withId(R.id.text))
        .perform(ViewActions.typeText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org"));

    onView(withId(R.id.text))
        .check(matches(withText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org")));
  }
}

package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.TextLayoutMode.Mode.REALISTIC;

import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.integration.axt.R;

/** Simple tests to verify espresso APIs can be used on both Robolectric and device. */
@RunWith(AndroidJUnit4.class)
@TextLayoutMode(REALISTIC)
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

    EditText editText = activity.findViewById(R.id.edit_text);

    assertThat(editText).isNotNull();
    assertThat(editText.isEnabled()).isTrue();
  }

  /** Perform the equivalent of launchActivityAndFindView_ById except using espresso APIs */
  @Test
  public void launchActivityAndFindView_espresso() throws Exception {
    onView(withId(R.id.edit_text)).check(matches(isCompletelyDisplayed()));
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

    onView(withId(R.id.button)).check(matches(isCompletelyDisplayed()));
    onView(withId(R.id.button)).perform(click());

    assertThat(activity.buttonClicked).isTrue();
  }

  /** Perform the 'traditional' mechanism of setting contents of a text view using findViewById */
  @Test
  @UiThreadTest
  public void typeText_findView() throws Exception {
    EspressoActivity activity = activityRule.getActivity();
    EditText editText = activity.findViewById(R.id.edit_text);
    editText.setText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org");

    assertThat(editText.getText().toString())
        .isEqualTo("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org");
  }

  /** Perform the equivalent of setText except using espresso APIs */
  @Test
  public void typeText_espresso() throws Exception {
    onView(withId(R.id.edit_text))
        .perform(typeText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org"));

    onView(withId(R.id.edit_text))
        .check(matches(withText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org")));
  }

  /** use typeText with a inputType phone */
  @Test
  public void typeText_phone() throws Exception {
    onView(withId(R.id.edit_text_phone)).perform(typeText("411"));

    onView(withId(R.id.edit_text_phone)).check(matches(withText("411")));
  }

  @Test
  public void textView() {
    onView(withText("Text View"))
        .check(
            (view, noViewFoundException) -> {
              assertThat(view.getWidth()).isGreaterThan(0);
              assertThat(view.getHeight()).isGreaterThan(0);
            });
    onView(withText("Text View")).check(matches(isCompletelyDisplayed()));
  }

  @Test
  public void customActivityLabel() {
    onView(withText("Activity Label")).check(matches(isCompletelyDisplayed()));
  }

  @Test
  public void changeText_withCloseSoftKeyboard() {
    // Type text and then press the button.
    onView(withId(R.id.edit_text)).perform(typeText("anything"), closeSoftKeyboard());

    // Check that the text was changed.
    onView(withId(R.id.edit_text)).check(matches(withText("anything")));
  }

  @Test
  public void changeText_addNewline() {
    onView(withId(R.id.edit_text)).perform(typeText("Some text."));
    onView(withId(R.id.edit_text)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
    onView(withId(R.id.edit_text)).perform(typeTextIntoFocusedView("Other text."));

    onView(withId(R.id.edit_text)).check(matches(withText("Some text.\nOther text.")));
  }
}

package org.robolectric.integrationtests.axt;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;

import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.integration.axt.R;

/** Simple tests to verify espresso APIs can be used on both Robolectric and device. */
@RunWith(AndroidJUnit4.class)
public final class EspressoTest {

  @Rule
  public ActivityScenarioRule<EspressoActivity> activityRule =
      new ActivityScenarioRule<>(EspressoActivity.class);

  @Test
  public void onIdle_doesnt_block() {
    Espresso.onIdle();
  }

  @Test
  public void launchActivityAndFindView_ById() {
    activityRule
        .getScenario()
        .onActivity(
            activity -> {
              EditText editText = activity.findViewById(R.id.edit_text);

              assertThat(editText).isNotNull();
              assertThat(editText.isEnabled()).isTrue();
            });
  }

  /** Perform the equivalent of launchActivityAndFindView_ById except using espresso APIs */
  @Test
  public void launchActivityAndFindView_espresso() {
    onView(withId(R.id.edit_text)).check(matches(isCompletelyDisplayed()));
  }

  /** Perform the 'traditional' mechanism of clicking a button in Robolectric using findViewById */
  @Test
  @UiThreadTest
  public void buttonClick() {
    activityRule
        .getScenario()
        .onActivity(
            activity -> {
              Button button = activity.findViewById(R.id.button);

              button.performClick();

              assertThat(activity.buttonClicked).isTrue();
            });
  }

  /** Perform the equivalent of click except using espresso APIs */
  @Test
  public void buttonClick_espresso() {
    // All methods within ActivityScenario are blocking calls, so the API requires us to run
    // them in the instrumentation thread. But ActivityScenario ActivityAction runs on main
    // thread, we should run Espresso checking in instrumentation thread.
    AtomicReference<EspressoActivity> activityRef = new AtomicReference<>();
    activityRule.getScenario().onActivity(activityRef::set);
    onView(withId(R.id.button)).check(matches(isCompletelyDisplayed()));
    onView(withId(R.id.button)).perform(click());
    // If we have clicked the button of EspressoActivity, we can get correct Activity
    // instance from ActivityScenario safely.
    assertThat(activityRef.get()).isNotNull();
    assertThat(activityRef.get().buttonClicked).isTrue();
  }

  /** Perform the 'traditional' mechanism of setting contents of a text view using findViewById */
  @Test
  @UiThreadTest
  public void typeText_findView() {
    activityRule
        .getScenario()
        .onActivity(
            activity -> {
              EditText editText = activity.findViewById(R.id.edit_text);
              editText.setText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org");

              assertThat(editText.getText().toString())
                  .isEqualTo("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org");
            });
  }

  /** Perform the equivalent of setText except using espresso APIs */
  @Test
  public void typeText_espresso() {
    onView(withId(R.id.edit_text))
        .perform(typeText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org"));

    onView(withId(R.id.edit_text))
        .check(matches(withText("\"new TEXT!#$%&'*+-/=?^_`{|}~@robolectric.org")));
  }

  /** use typeText with a inputType phone */
  @Test
  public void typeText_phone() {
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
  public void textViewWithPositiveScaleX() {
    onView(withId(R.id.text_view_positive_scale_x))
        .check(
            (view, noViewFoundException) -> {
              TextView textView = (TextView) view;
              float expectedTextScaleX = 1.5f;
              assertThat(textView.getTextScaleX()).isEqualTo(expectedTextScaleX);
              float scaledWidth = textView.getPaint().measureText(textView.getText().toString());
              textView.setTextScaleX(1f);
              float unscaledWidth = textView.getPaint().measureText(textView.getText().toString());
              assertThat(scaledWidth).isGreaterThan(unscaledWidth);
            });
  }

  @Test
  public void textViewWithNegativeScaleX() {
    onView(withId(R.id.text_view_negative_scale_x))
        .check(
            (view, noViewFoundException) -> {
              TextView textView = (TextView) view;
              assertThat(textView.getTextScaleX()).isEqualTo(-1.5f);
              float scaledWidth = textView.getPaint().measureText(textView.getText().toString());
              textView.setTextScaleX(1f);
              float unscaledWidth = textView.getPaint().measureText(textView.getText().toString());
              assertThat(scaledWidth).isLessThan(unscaledWidth);
            });
  }

  @Config(minSdk = LOLLIPOP)
  @SdkSuppress(minSdkVersion = LOLLIPOP)
  @Test
  public void textViewWithLetterSpacing() {
    onView(withId(R.id.text_view_letter_spacing))
        .check(
            (view, noViewFoundException) -> {
              TextView textView = (TextView) view;
              assertThat(textView.getLetterSpacing()).isEqualTo(0.05f);
            });
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

  @Test
  public void clickButton_after_swipeUp() {
    try (ActivityScenario<EspressoScrollingActivity> activityScenario =
        ActivityScenario.launch(EspressoScrollingActivity.class)) {
      onView(withId(R.id.scroll_view)).perform(swipeUp());
      onView(withId(R.id.button)).perform(click());
      activityScenario.onActivity(action -> assertThat(action.buttonClicked).isTrue());
    }
  }
}

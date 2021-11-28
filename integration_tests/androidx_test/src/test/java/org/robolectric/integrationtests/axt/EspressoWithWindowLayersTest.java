package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Root;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.integration.axt.R;

/** Test Espresso on Robolectric interoperability for toolbar menus. */
@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class EspressoWithWindowLayersTest {
  private static final String TEXT = "Hello World";

  /** The touchable popup window gets the event and the button is *not* clicked. */
  @Test
  public void click_interactivePopupWindow_isNotClicked() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      showInteractivePopupAsButtonDropdown(scenario);

      onView(withId(R.id.button)).inRoot(new IsBaseApplication()).perform(click());

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isFalse());
    }
  }

  /** The touchable popup window gets the event and the button is *not* clicked. */
  @Test
  public void click_occludingInteractivePopupWindow_isNotClicked() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      showOccludingInteractivePopupAsButtonDropdown(scenario);

      onView(withId(R.id.button)).inRoot(new IsBaseApplication()).perform(click());

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isFalse());
    }
  }

  /** The non-touchable popup window does *not* get the event and the button is clicked. */
  @Test
  public void click_nonInteractivePopupWindow_isClicked() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      showNonInteractivePopupAsButtonDropdown(scenario);

      onView(withId(R.id.button)).perform(click());

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isTrue());
    }
  }

  /** The focusable popup window gets the event and the text is *not* typed. */
  @Test
  public void typeText_interactivePopupWindow_textIsNotTyped() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      showOccludingInteractivePopupAsButtonDropdown(scenario);

      onView(withId(R.id.edit_text)).inRoot(new IsBaseApplication()).perform(typeText(TEXT));

      scenario.onActivity(
          activity -> {
            TextView tv = activity.findViewById(R.id.edit_text);
            assertThat(tv.getText().toString()).isNotEqualTo(TEXT);
          });
    }
  }

  /** The non-focusable popup window does *not* get the event and the text is typed. */
  @Test
  public void typeText_nonInteractivePopupWindow_textIsTyped() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      showNonInteractivePopupAsButtonDropdown(scenario);

      onView(withId(R.id.edit_text)).inRoot(new IsBaseApplication()).perform(typeText(TEXT));

      scenario.onActivity(
          activity -> {
            TextView tv = activity.findViewById(R.id.edit_text);
            assertThat(tv.getText().toString()).isEqualTo(TEXT);
          });
    }
  }

  /** Replacing text does not depend on events, so the focusable window does not interfere. */
  @Test
  public void replaceText_interactivePopupWindow_textIsReplaced() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      showOccludingInteractivePopupAsButtonDropdown(scenario);

      onView(withId(R.id.edit_text)).inRoot(new IsBaseApplication()).perform(replaceText(TEXT));

      scenario.onActivity(
          activity -> {
            TextView tv = activity.findViewById(R.id.edit_text);
            assertThat(tv.getText().toString()).isEqualTo(TEXT);
          });
    }
  }

  /** Replacing text does not depend on events, so the non-focusable window does not interfere. */
  @Test
  public void replaceText_nonInteractivePopupWindow_textIsReplaced() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      showNonInteractivePopupAsButtonDropdown(scenario);

      onView(withId(R.id.edit_text)).perform(replaceText(TEXT));

      scenario.onActivity(
          activity -> {
            TextView tv = activity.findViewById(R.id.edit_text);
            assertThat(tv.getText().toString()).isEqualTo(TEXT);
          });
    }
  }

  /**
   * Shows an occluding touchable and focusable popup window as a drop-down on the button.
   *
   * <p>The drop-down is shown *over* the button by adjusting the x and y offsets. The position of
   * the popup is *not* yet accounted for in the window selection heuristic. If it were, we should
   * see different behavior when attempting to click the button underneath.
   */
  private static void showOccludingInteractivePopupAsButtonDropdown(
      ActivityScenario<EspressoActivity> scenario) {
    scenario.onActivity(
        activity -> {
          View anchor = activity.findViewById(R.id.button);
          new PopupWindow(
                  /* contentView= */ new FrameLayout(activity),
                  /* width= */ anchor.getWidth() * 2,
                  /* height= */ anchor.getHeight() * 2,
                  /* focusable= */ true)
              .showAsDropDown(anchor, -anchor.getWidth(), -anchor.getHeight());
        });
  }

  /** Shows a non-occluding touchable and focusable popup window as a drop-down on the button. */
  private static void showInteractivePopupAsButtonDropdown(
      ActivityScenario<EspressoActivity> scenario) {
    scenario.onActivity(
        activity -> {
          View anchor = activity.findViewById(R.id.button);
          new PopupWindow(
                  /* contentView= */ new FrameLayout(activity),
                  /* width= */ 10,
                  /* height= */ 10,
                  /* focusable= */ true)
              .showAsDropDown(anchor);
        });
  }

  /**
   * Shows an occluding non-touchable and non-focusable popup window as a drop-down on the button.
   *
   * <p>The drop-down is shown *over* the button by adjusting the x and y offsets. The position of
   * the popup is *not* yet accounted for in the window selection heuristic. If it were, we should
   * see different behavior when attempting to click the button underneath.
   */
  private static void showNonInteractivePopupAsButtonDropdown(
      ActivityScenario<EspressoActivity> scenario) {
    scenario.onActivity(
        activity -> {
          View anchor = activity.findViewById(R.id.button);
          PopupWindow popup =
              new PopupWindow(
                  /* contentView= */ new FrameLayout(activity),
                  /* width= */ anchor.getWidth() * 2,
                  /* height= */ anchor.getHeight() * 2,
                  /* focusable= */ false);
          popup.setTouchable(false);
          popup.showAsDropDown(anchor, -anchor.getWidth(), -anchor.getHeight());
        });
  }

  /**
   * Espresso Root matcher for only windows with the base application window type.
   *
   * <p>This matcher is required as Espresso will dutifully default to finding views in the focused
   * window. However, the events are *not* guaranteed to be dispatched in this window. Robolectric
   * uses a different heuristic, so forcing this window to be used is good for testing.
   */
  static final class IsBaseApplication extends TypeSafeMatcher<Root> {
    @Override
    public void describeTo(Description description) {
      description.appendText("is the base application window");
    }

    @Override
    public boolean matchesSafely(Root root) {
      return root.getWindowLayoutParams().get().type
          == WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
    }
  }
}

package org.robolectric.integrationtests.axt;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.integration.axt.R;

/** Test Espresso on Robolectric interoperability for event dispatch with various window flags. */
@RunWith(AndroidJUnit4.class)
public class EspressoWithWindowLayersTest {
  private static final String TEXT = "Hello World";

  private static final int FOCUSABLE = 1;
  private static final int TOUCHABLE = 2;
  private static final int TOUCH_MODAL = 4;
  private static final int TOUCH_OUTSIDE = 8;

  private boolean popupTouchOutside;

  @Test
  public void click_notTouchablePopupOverButton_isClicked() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      showPopupOver(scenario, R.id.button, FOCUSABLE);

      onView(isRoot()).perform(clickAtLocation(centerOf(scenario, R.id.button)));

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isTrue());
    }
  }

  @Test
  public void click_touchablePopupOverButton_isNotClicked() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      showPopupOver(scenario, R.id.button, FOCUSABLE | TOUCHABLE);

      onView(isRoot()).perform(clickAtLocation(centerOf(scenario, R.id.button)));

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isFalse());
    }
  }

  @Test
  public void click_touchablePopupNotOverButton_isClicked() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      showPopupOver(scenario, R.id.edit_text, FOCUSABLE | TOUCHABLE);

      onView(isRoot()).perform(clickAtLocation(centerOf(scenario, R.id.button)));

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isTrue());
    }
  }

  @Test
  public void click_touchModalPopupNotOverButton_isNotClicked() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      showPopupOver(scenario, R.id.edit_text, FOCUSABLE | TOUCHABLE | TOUCH_MODAL);

      onView(isRoot()).perform(clickAtLocation(centerOf(scenario, R.id.button)));

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isFalse());
    }
  }

  @Test
  public void click_touchOutsidePopupNotOverButton_isClicked() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      showPopupOver(scenario, R.id.edit_text, FOCUSABLE | TOUCHABLE | TOUCH_OUTSIDE);

      onView(isRoot()).perform(clickAtLocation(centerOf(scenario, R.id.button)));

      scenario.onActivity(activity -> assertThat(activity.buttonClicked).isTrue());
      assertThat(popupTouchOutside).isTrue();
    }
  }

  @Test
  public void click_twoDialogs_clicksOnTopMost() {
    AtomicBoolean clicked = new AtomicBoolean();
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      scenario.onActivity(
          activity -> {
            new AlertDialog.Builder(activity)
                .setPositiveButton("Hello", (dialog, which) -> {})
                .create()
                .show();
            new AlertDialog.Builder(activity)
                .setPositiveButton("Hello", (dialog, which) -> clicked.set(true))
                .create()
                .show();
          });

      onView(withText("Hello")).inRoot(isDialog()).perform(click());

      assertThat(clicked.get()).isTrue();
    }
  }

  @Test
  public void typeText_focusablePopupWindow_textIsNotTyped() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      showPopupOver(scenario, R.id.button, FOCUSABLE);

      onView(withId(R.id.edit_text)).perform(typeText(TEXT));

      scenario.onActivity(
          activity -> assertThat(activity.editText.getText().toString()).isNotEqualTo(TEXT));
    }
  }

  @Test
  public void typeText_notFocusablePopupWindow_textIsTyped() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      showPopupOver(scenario, R.id.button, /* flags= */ 0);

      onView(withId(R.id.edit_text)).perform(typeText(TEXT));

      scenario.onActivity(
          activity -> assertThat(activity.editText.getText().toString()).isEqualTo(TEXT));
    }
  }

  /**
   * Shows a popup window entirely occluding the view identified by the view id in the activity. The
   * popup window will be cleared of its touch and focus flags so they reflect the flags configured.
   */
  private void showPopupOver(ActivityScenario<EspressoActivity> scenario, int viewId, int flags) {
    scenario.onActivity(
        activity -> {
          View view = activity.findViewById(viewId);
          int[] viewLocation = new int[2];
          view.getLocationOnScreen(viewLocation);

          // Create an edit text with the same id as the edit text in EspressoActivity so we can
          // test that the correct window receives the text input.
          EditText popupContentView = new EditText(view.getContext());
          popupContentView.setId(R.id.edit_text);

          PopupWindow popup = new PopupWindow(popupContentView, view.getWidth(), view.getHeight());
          popup.setFocusable((flags & FOCUSABLE) != 0);
          popup.setTouchable((flags & TOUCHABLE) != 0);
          popup.setTouchModal((flags & TOUCH_MODAL) != 0);
          popup.setOutsideTouchable((flags & TOUCH_OUTSIDE) != 0);
          // On sdk <=22 the touch interceptor is only used if the popup has a background configured
          popup.setBackgroundDrawable(new ColorDrawable(Color.RED));
          popup.setTouchInterceptor(
              (v, e) -> {
                if (e.getActionMasked() == MotionEvent.ACTION_OUTSIDE) {
                  popupTouchOutside = true;
                }
                return true;
              });
          popup.showAtLocation(view, Gravity.LEFT | Gravity.TOP, viewLocation[0], viewLocation[1]);
        });
  }

  /** Performs a click at the location from the coordinates provider (ignoring the matched view). */
  private static ViewAction clickAtLocation(CoordinatesProvider coordinatesProvider) {
    return new GeneralClickAction(
        Tap.SINGLE,
        coordinatesProvider,
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY);
  }

  /** Returns coordinates at the center of the view. */
  private static CoordinatesProvider centerOf(
      ActivityScenario<EspressoActivity> scenario, int viewId) {
    return v -> {
      AtomicReference<float[]> result = new AtomicReference<>();
      scenario.onActivity(
          activity ->
              result.set(
                  GeneralLocation.CENTER.calculateCoordinates(activity.findViewById(viewId))));
      return result.get();
    };
  }
}

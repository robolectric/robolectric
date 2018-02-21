package org.robolectric.integration_tests.atsl;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple tests to verify espresso APIs can be used on Robolectric.
 */
@RunWith(AndroidJUnit4.class)
public final class EspressoTest {

  /**
   * Test activity that contains a single TextView
   */ 
  public static class ActivityFixture extends Activity {

    private int viewId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      TextView textView = new TextView(this);
      this.viewId = generateViewId();
      textView.setId(viewId);
      textView.setEnabled(true);
      setContentView(textView);
    }

    private static int generateViewId() {
      if ( VERSION.SDK_INT >= 17) {
        return View.generateViewId();
      }
      return 0xbcbc;
    }
  }

  @Rule
  public ActivityTestRule<ActivityFixture> activityRule =
      new ActivityTestRule<>(ActivityFixture.class, false, true);

  @Test
  public void onIdle_doesnt_block() throws Exception {
    Espresso.onIdle();
  }

  @Test
  public void launchActivityAndFindView_ById() throws Exception {
    ActivityFixture activity = activityRule.getActivity();

    TextView textView = (TextView) activity.findViewById(activity.viewId);
    assertThat(textView).isNotNull();
    assertThat(textView.isEnabled()).isTrue();
  }

  /**
   * Perform the equivalent of launchActivityAndFindView_ById except using espresso APIs
   */
  @Test
  public void launchActivityAndFindView_espresso() throws Exception {
    ActivityFixture activity = activityRule.getActivity();
    onView(withId(activity.viewId)).check(matches(isEnabled()));
  }
}

package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.integration.axt.R;

@RunWith(AndroidJUnit4.class)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
public class EspressoWithSwitchCompat {
  @Test
  public void switchCompatTest() {
    ActivityScenario.launch(ActivityWithSwitchCompat.class);
    onView(withId(R.id.switch_compat_2)).check(matches(isCompletelyDisplayed())).perform(click());
  }
}

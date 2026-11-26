package org.robolectric.integrationtests.androidx;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.view.View;
import androidx.constraintlayout.widget.Group;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ConstraintLayoutTest {
  @Rule
  public ActivityScenarioRule<ConstraintLayoutActivity> activityRule =
      new ActivityScenarioRule<>(ConstraintLayoutActivity.class);

  /**
   * Tests for <a href="https://github.com/robolectric/robolectric/issues/4570">issue 4570</a> and
   * <a href="https://github.com/robolectric/robolectric/issues/4741">issue 4741</a>.
   */
  @Test
  public void viewsReferencesInGroupHaveTheCorrectVisibility() {
    activityRule.getScenario().onActivity(ConstraintLayoutActivity::showGroup);

    onView(withId(R.id.group))
        .check((view, noViewFoundException) -> {
          Group group = (Group) view;
          int[] referencedIds = group.getReferencedIds();
          assertArrayEquals(new int[] {R.id.text_1, R.id.text_2}, referencedIds);

          for (int referenceId : referencedIds) {
            assertEquals(
                View.VISIBLE,
                ((Activity) view.getContext()).findViewById(referenceId).getVisibility());
          }
        });
  }
}

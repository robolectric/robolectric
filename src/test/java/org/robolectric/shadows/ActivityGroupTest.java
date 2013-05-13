package org.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ActivityGroupTest {

  @Test
  public void getCurrentActivity_shouldReturnTheProvidedCurrentActivity() throws Exception {
  ActivityGroup activityGroup = new ActivityGroup();
  Activity activity = new Activity();
  shadowOf(activityGroup).setCurrentActivity(activity);

    assertThat(activityGroup.getCurrentActivity()).isSameAs(activity);
  }
}

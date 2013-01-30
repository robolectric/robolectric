package org.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityGroup;
import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ActivityGroupTest {

  @Test
  public void getCurrentActivity_shouldReturnTheProvidedCurrentActivity() throws Exception {
    ActivityGroup activityGroup = new ActivityGroup();
    Activity activity = new Activity();
    shadowOf(activityGroup).setCurrentActivity(activity);

    assertThat(activityGroup.getCurrentActivity(), is(activity));
  }
}

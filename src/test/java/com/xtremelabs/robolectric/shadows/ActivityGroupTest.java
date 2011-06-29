package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityGroup;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ActivityGroupTest {

  @Test
  public void getCurrentActivity_shouldReturnTheProvidedCurrentActivity() throws Exception {
    ActivityGroup activityGroup = new ActivityGroup();
    Activity activity = new Activity();
    shadowOf(activityGroup).setCurrentActivity(activity);

    assertThat(activityGroup.getCurrentActivity(), is(activity));
  }
}

package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.ActivityGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowActivityGroupTest {

  @Test
  public void getCurrentActivity_shouldReturnTheProvidedCurrentActivity() throws Exception {
  ActivityGroup activityGroup = new ActivityGroup();
  Activity activity = new Activity();
  shadowOf(activityGroup).setCurrentActivity(activity);

    assertThat(activityGroup.getCurrentActivity()).isSameAs(activity);
  }
}

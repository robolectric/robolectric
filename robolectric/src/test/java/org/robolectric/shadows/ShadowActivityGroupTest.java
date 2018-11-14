package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.ActivityGroup;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowActivityGroupTest {

  @Test
  public void getCurrentActivity_shouldReturnTheProvidedCurrentActivity() throws Exception {
  ActivityGroup activityGroup = new ActivityGroup();
  Activity activity = new Activity();
  shadowOf(activityGroup).setCurrentActivity(activity);

    assertThat(activityGroup.getCurrentActivity()).isSameAs(activity);
  }
}

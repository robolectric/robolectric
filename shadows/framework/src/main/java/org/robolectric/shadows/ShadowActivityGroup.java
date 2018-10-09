package org.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityGroup;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(ActivityGroup.class)
public class ShadowActivityGroup extends ShadowActivity {
  private Activity currentActivity;

  @Implementation
  protected android.app.Activity getCurrentActivity() {
    return currentActivity;
  }

  /**
   * @param activity Current activity.
   */
  public void setCurrentActivity(Activity activity) {
    currentActivity = activity;
  }
}

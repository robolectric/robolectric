package org.robolectric.util;

import org.robolectric.shadows.ShadowApplication;

// TODO: this will later be moved to robolectric-shadows to eliminate the depedency of robolectric -> robolectric-shadows
public class ShadowsAdapter {
  public Scheduler getBackgroundScheduler() {
    return ShadowApplication.getInstance().getBackgroundScheduler();
  }
}

package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

// TODO: this will later be moved to robolectric-shadows to eliminate the depedency of robolectric -> robolectric-shadows
public class ShadowsAdapter {
  public Scheduler getBackgroundScheduler() {
    return ShadowApplication.getInstance().getBackgroundScheduler();
  }

  public ShadowActivityAdapter getShadowActivityAdapter(Activity component) {
    return new ShadowActivityAdapter(component);
  }

  public static class ShadowActivityAdapter {
    private final ShadowActivity shadow;

    public ShadowActivityAdapter(Activity component) {
      this.shadow = Shadows.shadowOf(component);
    }

    public void setTestApplication(Application application) {
      shadow.setTestApplication(application);
    }

    public void setThemeFromManifest() {
      shadow.setThemeFromManifest();
    }
  }
}

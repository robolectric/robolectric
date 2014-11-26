package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import android.os.Looper;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

/**
 * This is the interface between the Robolectric runtime and the robolectric-shadows module.
 */
// TODO: this will later be moved to robolectric-shadows to eliminate the depedency of robolectric -> robolectric-shadows
public class ShadowsAdapter {
  public Scheduler getBackgroundScheduler() {
    return ShadowApplication.getInstance().getBackgroundScheduler();
  }

  public ShadowActivityAdapter getShadowActivityAdapter(Activity component) {
    return new ShadowActivityAdapter(component);
  }

  public ShadowLooperAdapter getMainLooper() {
    return new ShadowLooperAdapter(Looper.getMainLooper());
  }

  public String getShadowActivityThreadClassName() {
    return ShadowActivityThread.CLASS_NAME;
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

  public static class ShadowLooperAdapter {
    private final ShadowLooper shadow;

    public ShadowLooperAdapter(Looper looper) {
      this.shadow = Shadows.shadowOf(looper);
    }

    public void runPaused(Runnable runnable) {
      shadow.runPaused(runnable);
    }
  }
}

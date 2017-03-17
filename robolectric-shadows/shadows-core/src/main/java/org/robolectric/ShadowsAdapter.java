package org.robolectric;

import android.app.Activity;
import android.app.Application;

import org.robolectric.manifest.AndroidManifest;
import org.robolectric.android.Scheduler;

/**
 * Interface between robolectric and shadows-core modules.
 */
public interface ShadowsAdapter {
  Scheduler getBackgroundScheduler();

  ShadowActivityAdapter getShadowActivityAdapter(Activity component);

  ShadowLooperAdapter getMainLooper();

  String getShadowActivityThreadClassName();

  ShadowApplicationAdapter getApplicationAdapter(Activity component);

  void setupLogging();

  String getShadowContextImplClassName();

  void bind(Application application, AndroidManifest appManifest);

  interface ShadowActivityAdapter {

    void setThemeFromManifest();
  }

  interface ShadowLooperAdapter {
    void runPaused(Runnable runnable);
  }

  interface ShadowApplicationAdapter {
    AndroidManifest getAppManifest();
  }
}

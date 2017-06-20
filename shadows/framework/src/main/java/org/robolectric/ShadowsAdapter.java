package org.robolectric;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;

import org.robolectric.manifest.AndroidManifest;
import org.robolectric.util.Scheduler;

/**
 * Interface between robolectric and shadows-core modules.
 */
public interface ShadowsAdapter {
  Scheduler getBackgroundScheduler();

  ShadowActivityAdapter getShadowActivityAdapter(Activity component);

  ShadowLooperAdapter getMainLooper();

  // todo remove
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

package org.robolectric;

import android.app.Application;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.util.Scheduler;

/**
 * Interface between robolectric and shadows-framework modules.
 */
public interface ShadowsAdapter {
  Scheduler getBackgroundScheduler();

  ShadowLooperAdapter getMainLooper();

  // todo remove
  String getShadowActivityThreadClassName();

  void setupLogging();

  String getShadowContextImplClassName();

  void bind(Application application, AndroidManifest appManifest);

  interface ShadowLooperAdapter {
    void runPaused(Runnable runnable);
  }
}

package org.robolectric.shadows;

import android.app.Application;
import android.os.Looper;

import org.robolectric.Shadows;
import org.robolectric.ShadowsAdapter;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.util.Scheduler;

import static org.robolectric.Shadows.shadowOf;

/**
 * Interface between the Robolectric runtime and the shadows-core module.
 */
public class CoreShadowsAdapter implements ShadowsAdapter {
  @Override
  public Scheduler getBackgroundScheduler() {
    return ShadowApplication.getInstance().getBackgroundThreadScheduler();
  }

  @Override
  public ShadowLooperAdapter getMainLooper() {
    final ShadowLooper shadow = Shadows.shadowOf(Looper.getMainLooper());
    return new ShadowLooperAdapter() {
      public void runPaused(Runnable runnable) {
        shadow.runPaused(runnable);
      }
    };
  }

  @Override
  public void setupLogging() {
    ShadowLog.setupLogging();
  }

  @Override
  public void bind(Application application, AndroidManifest appManifest) {
    shadowOf(application).bind(appManifest);
  }
}

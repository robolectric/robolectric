package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.os.Looper;
import org.robolectric.Shadows;
import org.robolectric.ShadowsAdapter;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.util.Scheduler;

/**
 * Interface between the Robolectric runtime and the shadows-framework module.
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
      @Override public void runPaused(Runnable runnable) {
        shadow.runPaused(runnable);
      }
    };
  }

  @Override
  public String getShadowActivityThreadClassName() {
    return ShadowActivityThread.CLASS_NAME;
  }

  @Override
  public void setupLogging() {
    ShadowLog.setupLogging();
  }

  @Override
  public String getShadowContextImplClassName() {
    return ShadowContextImpl.CLASS_NAME;
  }

  @Override
  public void bind(Application application, AndroidManifest appManifest) {
    shadowOf(application).bind(appManifest);
  }
}

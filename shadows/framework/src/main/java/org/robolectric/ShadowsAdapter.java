package org.robolectric;

import android.app.Application;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.Scheduler;

/**
 * Interface between robolectric and shadows-framework modules.
 * @deprecated Prefer to access Shadow classes directly.
 */
@Deprecated
public interface ShadowsAdapter {

  /**
   * @deprecated Prefer {@link }org.robolectric.Robolectric#getBackgroundThreadScheduler}
   */
  @Deprecated
  Scheduler getBackgroundScheduler();

  /**
   * @deprecated Prefer {@link }shadowOf(Looper.getMainLooper()).runPaused()}
   */
  @Deprecated
  ShadowLooperAdapter getMainLooper();

  /**
   * @deprecated Prefer {@link ShadowActivityThread#CLASS_NAME} instead.
   */
  @Deprecated
  String getShadowActivityThreadClassName();

  /**
   * @deprecated Prefer {@link ShadowLog#setupLogging()} instead
   */
  @Deprecated
  void setupLogging();


  /**
   * @deprecated Prefer {@link ShadowContextImpl#CLASS_NAME} instead.
   */
  @Deprecated
  String getShadowContextImplClassName();

  /**
   * @deprecated Internal only
   */
  @Deprecated
  void bind(Application application, AndroidManifest appManifest);

  @Deprecated
  interface ShadowLooperAdapter {

    /**
     * @deprecated Prefer {@link }shadowOf(Looper.getMainLooper()).runPaused()}
     */
    @Deprecated
    void runPaused(Runnable runnable);
  }
}

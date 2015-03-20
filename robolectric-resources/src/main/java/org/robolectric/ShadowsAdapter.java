package org.robolectric;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.Scheduler;

/**
 * Interface between robolectric and shadows-core modules.
 */
public interface ShadowsAdapter {
  Scheduler getBackgroundScheduler();

  ShadowActivityAdapter getShadowActivityAdapter(Activity component);

  ShadowLooperAdapter getMainLooper();

  String getShadowActivityThreadClassName();

  void prepareShadowApplicationWithExistingApplication(Application application);

  ShadowApplicationAdapter getApplicationAdapter(Activity component);

  void setupLogging();

  String getShadowContextImplClassName();

  void setSystemResources(ResourceLoader systemResourceLoader);

  void overrideQualifiers(Configuration configuration, String qualifiers);

  void bind(Application application, AndroidManifest appManifest, ResourceLoader resourceLoader);

  void setPackageName(Application application, String packageName);

  void setAssetsQualifiers(AssetManager assets, String qualifiers);

  ResourceLoader getResourceLoader();

  interface ShadowActivityAdapter {
    void setTestApplication(Application application);

    void setThemeFromManifest();
  }

  interface ShadowLooperAdapter {
    void runPaused(Runnable runnable);
  }

  interface ShadowApplicationAdapter {
    AndroidManifest getAppManifest();

    ResourceLoader getResourceLoader();
  }
}

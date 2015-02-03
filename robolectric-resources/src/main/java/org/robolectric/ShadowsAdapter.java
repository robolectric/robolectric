package org.robolectric;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.Scheduler;

import java.io.File;

/**
 * Interface between robolectric and shadows-core modules.
 *
 * TODO: Move this interface into robolectric-utils and remove dependency on robolectric-resources.
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

  public interface ShadowActivityAdapter {
    public void setTestApplication(Application application);

    public void setThemeFromManifest();
  }

  public interface ShadowLooperAdapter {
    public void runPaused(Runnable runnable);
  }

  public interface ShadowApplicationAdapter {
    public AndroidManifest getAppManifest();

    public ResourceLoader getResourceLoader();
  }
}

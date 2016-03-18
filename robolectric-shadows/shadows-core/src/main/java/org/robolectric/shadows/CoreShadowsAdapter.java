package org.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Looper;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.ShadowsAdapter;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceLoader;
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
  public ShadowActivityAdapter getShadowActivityAdapter(Activity component) {
    final ShadowActivity shadow = Shadows.shadowOf(component);
    return new ShadowActivityAdapter() {

      public void setThemeFromManifest() {
        shadow.setThemeFromManifest();
      }
    };
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
  public String getShadowActivityThreadClassName() {
    return ShadowActivityThread.CLASS_NAME;
  }

  @Override
  public ShadowApplicationAdapter getApplicationAdapter(final Activity component) {
    return new ShadowApplicationAdapter() {
      public AndroidManifest getAppManifest() {
        return ShadowApplication.getInstance().getAppManifest();
      }

      public ResourceLoader getResourceLoader() {
        return shadowOf(RuntimeEnvironment.application.getAssets()).getResourceLoader();
      }
    };
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
  public void setSystemResources(ResourceLoader systemResourceLoader) {
    ShadowAssetManager.setSystemResources(systemResourceLoader);
  }

  @Override
  public void overrideQualifiers(Configuration configuration, String qualifiers) {
    shadowOf(configuration).overrideQualifiers(qualifiers);
  }

  @Override
  public void bind(Application application, AndroidManifest appManifest, ResourceLoader resourceLoader) {
    shadowOf(application).bind(appManifest, resourceLoader);
  }

  @Override
  public void setAssetsQualifiers(AssetManager assets, String qualifiers) {
    shadowOf(assets).setQualifiers(qualifiers);
  }

  @Override
  public ResourceLoader getResourceLoader() {
    return shadowOf(RuntimeEnvironment.application.getAssets()).getResourceLoader();
  }

}

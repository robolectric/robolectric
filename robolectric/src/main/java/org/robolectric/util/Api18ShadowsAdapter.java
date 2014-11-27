package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Looper;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowResources;

import static org.robolectric.Shadows.shadowOf;

/**
 * This is the interface between the Robolectric runtime and the robolectric-shadows module.
 */
// TODO: this will later be moved to robolectric-shadows to eliminate the depedency of robolectric -> robolectric-shadows
public class Api18ShadowsAdapter implements ShadowsAdapter {
  @Override
  public Scheduler getBackgroundScheduler() {
    return ShadowApplication.getInstance().getBackgroundScheduler();
  }

  @Override
  public ShadowActivityAdapter getShadowActivityAdapter(Activity component) {
    final ShadowActivity shadow = Shadows.shadowOf(component);
    return new ShadowActivityAdapter() {
      public void setTestApplication(Application application) {
        shadow.setTestApplication(application);
      }

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
  public void prepareShadowApplicationWithExistingApplication(Application application) {
    ShadowApplication roboShadow = Shadows.shadowOf(RuntimeEnvironment.application);
    ShadowApplication testShadow = Shadows.shadowOf(application);
    testShadow.bind(roboShadow.getAppManifest(), roboShadow.getResourceLoader());
    testShadow.callAttachBaseContext(RuntimeEnvironment.application.getBaseContext());
  }

  @Override
  public ShadowApplicationAdapter getApplicationAdapter(Activity component) {
    final ShadowApplication shadow = Shadows.shadowOf(component.getApplication());
    return new ShadowApplicationAdapter() {
      public AndroidManifest getAppManifest() {
        return shadow.getAppManifest();
      }

      public ResourceLoader getResourceLoader() {
        return shadow.getResourceLoader();
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
    ShadowResources.setSystemResources(systemResourceLoader);
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
  public void setPackageName(Application application, String packageName) {
    shadowOf(application).setPackageName(packageName);
  }

  @Override
  public void setAssetsQualifiers(AssetManager assets, String qualifiers) {
    shadowOf(assets).setQualifiers(qualifiers);
  }

  @Override
  public void reset() {
    Shadows.reset();
  }
}

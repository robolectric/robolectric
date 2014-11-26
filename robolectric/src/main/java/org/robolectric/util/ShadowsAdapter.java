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
public class ShadowsAdapter {
  public Scheduler getBackgroundScheduler() {
    return ShadowApplication.getInstance().getBackgroundScheduler();
  }

  public ShadowActivityAdapter getShadowActivityAdapter(Activity component) {
    return new ShadowActivityAdapter(component);
  }

  public ShadowLooperAdapter getMainLooper() {
    return new ShadowLooperAdapter(Looper.getMainLooper());
  }

  public String getShadowActivityThreadClassName() {
    return ShadowActivityThread.CLASS_NAME;
  }

  public void prepareShadowApplicationWithExistingApplication(Application application) {
    ShadowApplication roboShadow = Shadows.shadowOf(RuntimeEnvironment.application);
    ShadowApplication testShadow = Shadows.shadowOf(application);
    testShadow.bind(roboShadow.getAppManifest(), roboShadow.getResourceLoader());
    testShadow.callAttachBaseContext(RuntimeEnvironment.application.getBaseContext());
  }

  public ShadowApplicationAdapter getApplicationAdapter(Activity component) {
    return new ShadowApplicationAdapter(component.getApplication());
  }

  public void setupLogging() {
    ShadowLog.setupLogging();
  }

  public String getShadowContextImplClassName() {
    return ShadowContextImpl.CLASS_NAME;
  }

  public void setSystemResources(ResourceLoader systemResourceLoader) {
    ShadowResources.setSystemResources(systemResourceLoader);
  }

  public void overrideQualifiers(Configuration configuration, String qualifiers) {
    shadowOf(configuration).overrideQualifiers(qualifiers);
  }

  public void bind(Application application, AndroidManifest appManifest, ResourceLoader resourceLoader) {
    shadowOf(application).bind(appManifest, resourceLoader);
  }

  public void setPackageName(Application application, String packageName) {
    shadowOf(application).setPackageName(packageName);
  }

  public void setAssetsQualifiers(AssetManager assets, String qualifiers) {
    shadowOf(assets).setQualifiers(qualifiers);
  }

  public static class ShadowActivityAdapter {
    private final ShadowActivity shadow;

    public ShadowActivityAdapter(Activity component) {
      this.shadow = Shadows.shadowOf(component);
    }

    public void setTestApplication(Application application) {
      shadow.setTestApplication(application);
    }

    public void setThemeFromManifest() {
      shadow.setThemeFromManifest();
    }
  }

  public static class ShadowLooperAdapter {
    private final ShadowLooper shadow;

    public ShadowLooperAdapter(Looper looper) {
      this.shadow = Shadows.shadowOf(looper);
    }

    public void runPaused(Runnable runnable) {
      shadow.runPaused(runnable);
    }
  }

  public static class ShadowApplicationAdapter {
    private final ShadowApplication shadow;

    public ShadowApplicationAdapter(Application application) {
      this.shadow = Shadows.shadowOf(application);
    }

    public AndroidManifest getAppManifest() {
      return shadow.getAppManifest();
    }

    public ResourceLoader getResourceLoader() {
      return shadow.getResourceLoader();
    }
  }
}

package org.robolectric;

import java.lang.reflect.Method;
import javax.inject.Named;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.BootstrapWrapperI;
import org.robolectric.android.internal.AndroidTestEnvironment;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/** Wrapper for testing use of AndroidTestEnvironment. */
public class BootstrapWrapper extends AndroidTestEnvironment implements BootstrapWrapperI {
  public AndroidTestEnvironment wrappedTestEnvironment;
  public boolean legacyResources;
  public Method method;
  public Configuration config;
  public AndroidManifest appManifest;

  public BootstrapWrapper(
      @Named("runtimeSdk") Sdk runtimeSdk,
      @Named("compileSdk") Sdk compileSdk,
      ResourcesMode resourcesMode, ApkLoader apkLoader,
      ShadowProvider[] shadowProviders,
      TestEnvironmentLifecyclePlugin[] lifecyclePlugins) {
    super(runtimeSdk, compileSdk, resourcesMode, apkLoader, shadowProviders, lifecyclePlugins);
    this.wrappedTestEnvironment = new AndroidTestEnvironment(runtimeSdk, compileSdk, resourcesMode,
        apkLoader, shadowProviders, lifecyclePlugins);
  }

  @Override
  public void setUpApplicationState(Method method, Configuration config,
      AndroidManifest appManifest) {
    this.method = method;
    this.config = config;
    this.appManifest = appManifest;

    BootstrapDeferringRobolectricTestRunner.bootstrapWrapperInstance = this;
  }

  @Override
  public void tearDownApplication() {
    wrappedTestEnvironment.tearDownApplication();
  }

  @Override
  public void callSetUpApplicationState() {
    wrappedTestEnvironment.setUpApplicationState(method, config, appManifest);
  }

  @Override
  public void changeConfig(Configuration config) {
    this.config = config;
  }

  @Override
  public boolean isLegacyResources() {
    return legacyResources;
  }

  @Override
  public AndroidManifest getAppManifest() {
    return appManifest;
  }

  @Override
  public void changeAppManifest(AndroidManifest manifest) {
    this.appManifest = manifest;
  }

  @Override
  public void resetState() {
    wrappedTestEnvironment.resetState();
  }
}

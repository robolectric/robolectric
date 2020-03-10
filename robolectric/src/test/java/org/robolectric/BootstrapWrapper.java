package org.robolectric;

import javax.inject.Named;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.BootstrapWrapperI;
import org.robolectric.android.internal.AndroidTestEnvironment;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin;
import org.robolectric.pluginapi.config.Configuration;
import org.robolectric.plugins.ConfigurationImpl;

/** Wrapper for testing use of AndroidTestEnvironment. */
public class BootstrapWrapper extends AndroidTestEnvironment implements BootstrapWrapperI {
  public AndroidTestEnvironment wrappedTestEnvironment;
  public boolean legacyResources;
  public Configuration configuration;
  private String testName;

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
  public void setUpApplicationState(Configuration configuration, String testName) {
    this.configuration = configuration;
    this.testName = testName;

    BootstrapDeferringRobolectricTestRunner.bootstrapWrapperInstance = this;
  }

  @Override
  public void callSetUpApplicationState() {
    wrappedTestEnvironment.setUpApplicationState(configuration, testName);
  }

  @Override
  public Configuration getConfig() {
    return configuration;
  }

  @Override
  public void changeConfig(Configuration config) {
    this.configuration = config;
  }

  @Override
  public boolean isLegacyResources() {
    return legacyResources;
  }

  @Override
  public void changeAppManifest(AndroidManifest manifest) {
    this.configuration = new ConfigurationImpl(configuration)
        .put(AndroidManifest.class, manifest);
  }

  @Override
  public void tearDownApplication() {
    wrappedTestEnvironment.tearDownApplication();
  }
}

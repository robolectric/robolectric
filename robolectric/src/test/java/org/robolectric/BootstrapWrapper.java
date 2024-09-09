package org.robolectric;

import javax.inject.Named;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.BootstrapWrapperI;
import org.robolectric.android.internal.AndroidTestEnvironment;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/** Wrapper for testing use of AndroidTestEnvironment. */
public class BootstrapWrapper extends AndroidTestEnvironment implements BootstrapWrapperI {
  public AndroidTestEnvironment wrappedTestEnvironment;
  public boolean legacyResources;
  public String tmpDirName;
  public Configuration config;
  public AndroidManifest appManifest;

  public BootstrapWrapper(
      @Named("runtimeSdk") Sdk runtimeSdk,
      @Named("compileSdk") Sdk compileSdk,
      ShadowProvider[] shadowProviders,
      TestEnvironmentLifecyclePlugin[] lifecyclePlugins) {
    super(runtimeSdk, compileSdk, shadowProviders, lifecyclePlugins);
    this.wrappedTestEnvironment =
        new AndroidTestEnvironment(runtimeSdk, compileSdk, shadowProviders, lifecyclePlugins);
  }

  @Override
  public void setUpApplicationState(
      String tmpDirName, Configuration config, AndroidManifest appManifest) {
    this.tmpDirName = tmpDirName;
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
    wrappedTestEnvironment.setUpApplicationState(tmpDirName, config, appManifest);
  }

  @Override
  public void changeConfig(Configuration config) {
    this.config = config;
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

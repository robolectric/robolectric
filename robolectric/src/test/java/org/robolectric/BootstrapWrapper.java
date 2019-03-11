package org.robolectric;

import java.lang.reflect.Method;
import javax.inject.Named;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.BootstrapWrapperI;
import org.robolectric.android.internal.AndroidEnvironment;
import org.robolectric.internal.Environment;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/** Wrapper for testing use of AndroidEnvironment. */
public class BootstrapWrapper extends AndroidEnvironment implements BootstrapWrapperI {
  public AndroidEnvironment wrapped;
  public boolean legacyResources;
  public Method method;
  public Configuration config;
  public AndroidManifest appManifest;

  public BootstrapWrapper(
      @Named("runtimeSdk") Sdk runtimeSdk,
      @Named("compileSdk") Sdk compileSdk,
      ResourcesMode resourcesMode, ApkLoader apkLoader,
      ShadowProvider[] shadowProviders) {
    super(runtimeSdk, compileSdk, resourcesMode, apkLoader, shadowProviders);
    this.wrapped = new AndroidEnvironment(runtimeSdk, compileSdk, resourcesMode, apkLoader,
        shadowProviders);
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
    wrapped.tearDownApplication();
  }

  @Override
  public Environment getWrapped() {
    return wrapped;
  }

  @Override
  public void callSetUpApplicationState() {
    wrapped.setUpApplicationState(method, config, appManifest);
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
}

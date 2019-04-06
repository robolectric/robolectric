package org.robolectric;

import java.lang.reflect.Method;
import javax.inject.Named;
import org.robolectric.BootstrapDeferringRobolectricTestRunner.BootstrapWrapperI;
import org.robolectric.android.internal.AndroidTestEnvironment;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin;
import org.robolectric.pluginapi.config.ConfiguredTest;

/** Wrapper for testing use of AndroidTestEnvironment. */
public class BootstrapWrapper extends AndroidTestEnvironment implements BootstrapWrapperI {
  public AndroidTestEnvironment wrappedTestEnvironment;
  public Method method;
  public ConfiguredTest configuredTest;

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
  public void before(ConfiguredTest configuredTest) {
    this.configuredTest = configuredTest;

    BootstrapDeferringRobolectricTestRunner.bootstrapWrapperInstance = this;
  }

  @Override
  public void callBefore() {
    wrappedTestEnvironment.before(configuredTest);
  }

  @Override
  public void after(ConfiguredTest configuredTest) {
  }

  @Override
  public void callAfter() {
    wrappedTestEnvironment.after(configuredTest);
  }

  @Override
  public ConfiguredTest getConfiguredTest() {
    return configuredTest;
  }

  @Override
  public void changeConfiguredTest(ConfiguredTest configuredTest) {
    this.configuredTest = configuredTest;
  }

}

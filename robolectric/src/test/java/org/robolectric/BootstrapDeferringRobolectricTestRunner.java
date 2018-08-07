package org.robolectric;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentationConfiguration.Builder;
import org.robolectric.manifest.AndroidManifest;

/**
 * Test runner which prevents full initialization (bootstrap) of the Android process at test setup.
 */
public class BootstrapDeferringRobolectricTestRunner extends RobolectricTestRunner {

  private static BootstrapWrapper bootstrapWrapper;

  public BootstrapDeferringRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Nonnull
  @Override
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return MyTestLifecycle.class;
  }

  @Override
  ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
    bootstrapWrapper = new BootstrapWrapper(super.getHooksInterface(sdkEnvironment));
    return bootstrapWrapper;
  }

  @Nonnull
  @Override
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    return new Builder(super.createClassLoaderConfig(method))
        .doNotAcquireClass(BootstrapDeferringRobolectricTestRunner.class)
        .doNotAcquireClass(RoboInject.class)
        .doNotAcquireClass(MyTestLifecycle.class)
        .doNotAcquireClass(BootstrapWrapper.class)
        .build();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface RoboInject {
  }

  public static class MyTestLifecycle extends DefaultTestLifecycle {
    @Override
    public void prepareTest(Object test) {
      super.prepareTest(test);
      for (Field field : test.getClass().getDeclaredFields()) {
        if (field.getAnnotation(RoboInject.class) != null) {
          if (field.getType().isAssignableFrom(BootstrapWrapper.class)) {
            field.setAccessible(true);
            try {
              field.set(test, bootstrapWrapper);
            } catch (IllegalAccessException e) {
              throw new RuntimeException("can't set " + field, e);
            }
          }
        }
      }
    }
  }

  public static class BootstrapWrapper implements ParallelUniverseInterface {
    public ParallelUniverseInterface hooksInterface;
    public boolean legacyResources;
    public ApkLoader apkLoader;
    public Method method;
    public Config config;
    public AndroidManifest appManifest;
    public SdkEnvironment sdkEnvironment;

    public BootstrapWrapper(ParallelUniverseInterface hooksInterface) {
      this.hooksInterface = hooksInterface;
    }

    @Override
    public void setSdkConfig(SdkConfig sdkConfig) {
      hooksInterface.setSdkConfig(sdkConfig);
    }

    @Override
    public void setResourcesMode(boolean legacyResources) {
      hooksInterface.setResourcesMode(legacyResources);
      this.legacyResources = legacyResources;
    }

    @Override
    public void setUpApplicationState(ApkLoader apkLoader, Method method, Config config,
        AndroidManifest appManifest, SdkEnvironment sdkEnvironment) {
      this.apkLoader = apkLoader;
      this.method = method;
      this.config = config;
      this.appManifest = appManifest;
      this.sdkEnvironment = sdkEnvironment;
    }

    @Override
    public Thread getMainThread() {
      return hooksInterface.getMainThread();
    }

    @Override
    public void setMainThread(Thread newMainThread) {
      hooksInterface.setMainThread(newMainThread);
    }

    @Override
    public void tearDownApplication() {
      hooksInterface.tearDownApplication();
    }

    @Override
    public Object getCurrentApplication() {
      return hooksInterface.getCurrentApplication();
    }

    public void callSetUpApplicationState() {
      hooksInterface.setUpApplicationState(apkLoader, method, config, appManifest, sdkEnvironment);
    }
  }
}

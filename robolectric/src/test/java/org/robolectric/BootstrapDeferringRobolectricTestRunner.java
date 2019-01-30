package org.robolectric;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.inject.Named;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.android.internal.AndroidEnvironment;
import org.robolectric.internal.Environment;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.SandboxFactory;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentationConfiguration.Builder;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.util.inject.Injector;

/**
 * Test runner which prevents full initialization (bootstrap) of the Android process at test setup.
 */
public class BootstrapDeferringRobolectricTestRunner extends RobolectricTestRunner {

  private static final Injector DEFAULT_INJECTOR = defaultInjector().build();

  public static BootstrapWrapperI bootstrapWrapperInstance = null;

  protected static Injector.Builder defaultInjector() {
    return RobolectricTestRunner.defaultInjector()
        .bind(SandboxFactory.class, BootstrapWrapperSandboxFactory.class);
  }

  public BootstrapDeferringRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass, DEFAULT_INJECTOR);
  }

  @Nonnull
  @Override
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return MyTestLifecycle.class;
  }

  public static class BootstrapWrapperSandboxFactory extends SandboxFactory {

    public BootstrapWrapperSandboxFactory(Injector injector, SdkCollection sdkCollection) {
      super(injector, sdkCollection);
    }

    @Override
    protected Class<? extends Environment> getEnvironmentClass() {
      return BootstrapWrapper.class;
    }
  }

  @Nonnull
  @Override
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    return new Builder(super.createClassLoaderConfig(method))
        .doNotAcquireClass(BootstrapDeferringRobolectricTestRunner.class)
        .doNotAcquireClass(RoboInject.class)
        .doNotAcquireClass(MyTestLifecycle.class)
        .doNotAcquireClass(BootstrapWrapperI.class)
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
          if (field.getType().isAssignableFrom(BootstrapWrapperI.class)) {
            field.setAccessible(true);
            try {
              field.set(test, bootstrapWrapperInstance);
            } catch (IllegalAccessException e) {
              throw new RuntimeException("can't set " + field, e);
            }
          }
        }
      }
    }
  }

  public interface BootstrapWrapperI {
    Environment getWrapped();

    void callSetUpApplicationState();

    void changeConfig(Configuration config);

    boolean isLegacyResources();

    AndroidManifest getAppManifest();

    void changeAppManifest(AndroidManifest manifest);

    void tearDownApplication();
  }

  public static class BootstrapWrapper implements Environment, BootstrapWrapperI {
    public AndroidEnvironment wrapped;
    public boolean legacyResources;
    public Method method;
    public Configuration config;
    public AndroidManifest appManifest;

    public BootstrapWrapper(
        @Named("runtimeSdk") Sdk runtimeSdk,
        @Named("compileSdk") Sdk compileSdk,
        ResourcesMode resourcesMode, ApkLoader apkLoader) {
      this.wrapped = new AndroidEnvironment(runtimeSdk, compileSdk, resourcesMode, apkLoader);
    }

    @Override
    public void setUpApplicationState(Method method, Configuration config,
        AndroidManifest appManifest) {
      this.method = method;
      this.config = config;
      this.appManifest = appManifest;

      bootstrapWrapperInstance = this;
    }

    @Override
    public void tearDownApplication() {
      wrapped.tearDownApplication();
    }

    @Override
    public Environment getWrapped() {
      return wrapped;
    }

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
}

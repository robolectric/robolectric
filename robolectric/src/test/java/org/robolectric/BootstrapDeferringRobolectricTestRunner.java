package org.robolectric;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.internal.AndroidSandbox.TestEnvironmentSpec;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.util.inject.Injector;

/**
 * Test runner which prevents full initialization (bootstrap) of the Android process at test setup.
 */
public class BootstrapDeferringRobolectricTestRunner extends RobolectricTestRunner {

  private static final Injector DEFAULT_INJECTOR = defaultInjector().build();

  public static BootstrapWrapperI bootstrapWrapperInstance = null;

  protected static Injector.Builder defaultInjector() {
    return RobolectricTestRunner.defaultInjector()
        .bind(TestEnvironmentSpec.class, new TestEnvironmentSpec(BootstrapWrapper.class));
  }

  public BootstrapDeferringRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass, DEFAULT_INJECTOR);
  }

  @Nonnull
  @Override
  protected Class<MyTestLifecycle> getTestLifecycleClass() {
    return MyTestLifecycle.class;
  }

  @Nonnull
  @Override
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    return new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
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

    void callSetUpApplicationState();

    void changeConfig(Configuration config);

    boolean isLegacyResources();

    AndroidManifest getAppManifest();

    void changeAppManifest(AndroidManifest manifest);

    void tearDownApplication();

    void resetState();
  }

}

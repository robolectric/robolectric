package org.robolectric;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SandboxFactory;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentationConfiguration.Builder;
import org.robolectric.manifest.AndroidManifest;

/**
 * Test runner which prevents full initialization (bootstrap) of the Android process at test setup.
 */
public class BootstrapDeferringRobolectricTestRunner extends RobolectricTestRunner {

  private static SoftReference<SandboxFactory> SANDBOX_FACTORY = new SoftReference<>(null);
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
  protected SandboxFactory getSandboxFactory() {
    SandboxFactory sandboxFactory = SANDBOX_FACTORY.get();
    if (sandboxFactory == null) {
      sandboxFactory = new MySandboxFactory();
      SANDBOX_FACTORY = new SoftReference<>(sandboxFactory);
    }
    return sandboxFactory;
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
      inject(test, BootstrapWrapper.class, bootstrapWrapper);
    }

    private <T> void inject(Object instance, Class<T> clazz, T value) {
      for (Field field : instance.getClass().getDeclaredFields()) {
        if (field.getAnnotation(RoboInject.class) != null) {
          if (field.getType().isAssignableFrom(clazz)) {
            field.setAccessible(true);
            try {
              field.set(instance, value);
            } catch (IllegalAccessException e) {
              throw new RuntimeException("can't set " + field, e);
            }
          }
        }
      }
    }
  }

  public static class BootstrapWrapper implements ParallelUniverseInterface {

    public ParallelUniverseInterface delegate;
    public ApkLoader apkLoader;
    public Method method;
    public Config config;
    public AndroidManifest appManifest;
    public SdkEnvironment sdkEnvironment;

    public BootstrapWrapper(ParallelUniverseInterface delegate) {
      this.delegate = delegate;
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

    public void callSetUpApplicationState() {
      delegate.setUpApplicationState(apkLoader, method, config, appManifest, sdkEnvironment);
    }

    @Override
    public void tearDownApplication() {
      delegate.tearDownApplication();
    }

    @Override
    public Object getCurrentApplication() {
      return delegate.getCurrentApplication();
    }
  }

  private static class MySandboxFactory extends SandboxFactory {

    @Override
    protected SdkEnvironment createSdkEnvironment(SdkConfig sdkConfig, boolean useLegacyResources,
        ClassLoader robolectricClassLoader) {
      return new MySdkEnvironment(sdkConfig, useLegacyResources, robolectricClassLoader);
    }
  }

  private static class MySdkEnvironment extends SdkEnvironment {

    private BootstrapWrapper myBootstrapWrapper;

    MySdkEnvironment(SdkConfig sdkConfig, boolean useLegacyResources, ClassLoader classLoader) {
      super(sdkConfig, useLegacyResources, classLoader);
    }

    @Override
    public void executeSynchronously(Runnable runnable) {
      BootstrapDeferringRobolectricTestRunner.bootstrapWrapper = myBootstrapWrapper;
      try {
        super.executeSynchronously(runnable);
      } finally {
        BootstrapDeferringRobolectricTestRunner.bootstrapWrapper = null;
      }
    }

    @Override
    public <V> V executeSynchronously(Callable<V> callable) throws Exception {
      BootstrapDeferringRobolectricTestRunner.bootstrapWrapper = myBootstrapWrapper;
      try {
        return super.executeSynchronously(callable);
      } finally {
        BootstrapDeferringRobolectricTestRunner.bootstrapWrapper = null;
      }
    }

    @Override
    protected ParallelUniverseInterface getParallelUniverse() {
      ParallelUniverseInterface parallelUniverse = super.getParallelUniverse();
      try {
        ParallelUniverseInterface wrapper = bootstrappedClass(BootstrapWrapper.class)
            .asSubclass(ParallelUniverseInterface.class)
            .getConstructor(ParallelUniverseInterface.class)
            .newInstance(parallelUniverse);
        myBootstrapWrapper = (BootstrapWrapper) wrapper;
        return wrapper;
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

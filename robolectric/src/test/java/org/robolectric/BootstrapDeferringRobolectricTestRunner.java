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
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.Bridge;
import org.robolectric.internal.SandboxFactory;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentationConfiguration.Builder;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.util.inject.Injector;

/**
 * Test runner which prevents full initialization (bootstrap) of the Android process at test setup.
 */
public class BootstrapDeferringRobolectricTestRunner extends RobolectricTestRunner {

  private static SoftReference<Injector> INJECTOR =
      new SoftReference<>(null);

  private static BootstrapWrapper bootstrapWrapper;

  public BootstrapDeferringRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass, getInjector());
  }

  @Nonnull
  @Override
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return MyTestLifecycle.class;
  }

  private static Injector getInjector() {
    Injector injector = INJECTOR.get();
    if (injector == null) {
      injector = defaultInjector().register(SandboxFactory.class, MySandboxFactory.class);
      INJECTOR = new SoftReference<>(injector);
    }
    return injector;
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

  public static class BootstrapWrapper implements Bridge {

    public Bridge delegate;
    public Method method;
    public Config config;
    public AndroidManifest appManifest;
    public AndroidSandbox androidSandbox;

    public BootstrapWrapper(Bridge delegate) {
      this.delegate = delegate;
    }

    @Override
    public void setUpApplicationState(Method method, Config config,
        AndroidManifest appManifest, AndroidSandbox androidSandbox) {
      this.method = method;
      this.config = config;
      this.appManifest = appManifest;
      this.androidSandbox = androidSandbox;
    }

    public void callSetUpApplicationState() {
      delegate.setUpApplicationState(method, config, appManifest, androidSandbox);
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

    private final ApkLoader apkLoader;

    public MySandboxFactory(DependencyResolver dependencyResolver,
        SdkProvider sdkProvider, ApkLoader apkLoader) {
      super(dependencyResolver, sdkProvider, apkLoader);
      this.apkLoader = apkLoader;
    }

    @Override
    protected AndroidSandbox createSandbox(SdkConfig sdkConfig, boolean useLegacyResources,
        ClassLoader robolectricClassLoader) {
      return new MyAndroidSandbox(sdkConfig, useLegacyResources, robolectricClassLoader, apkLoader);
    }
  }

  private static class MyAndroidSandbox extends AndroidSandbox {

    private BootstrapWrapper myBootstrapWrapper;

    MyAndroidSandbox(SdkConfig sdkConfig, boolean useLegacyResources, ClassLoader classLoader,
        ApkLoader apkLoader) {
      super(sdkConfig, useLegacyResources, classLoader, apkLoader);
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
    protected Bridge getBridge() {
      Bridge parallelUniverse = super.getBridge();
      try {
        Bridge wrapper = bootstrappedClass(BootstrapWrapper.class)
            .asSubclass(Bridge.class)
            .getConstructor(Bridge.class)
            .newInstance(parallelUniverse);
        myBootstrapWrapper = (BootstrapWrapper) wrapper;
        return wrapper;
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

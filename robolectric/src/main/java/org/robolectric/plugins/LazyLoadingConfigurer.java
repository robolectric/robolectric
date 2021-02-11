package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import org.robolectric.annotation.LazyLoadApplication;
import org.robolectric.annotation.LazyLoadApplication.LazyLoad;
import org.robolectric.pluginapi.config.Configurer;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} that reads the {@link LazyLoadApplication}
 * to dictate whether Robolectric should lazily instantiate the Application under test (as well as
 * the test Instrumentation).
 */
@AutoService(Configurer.class)
public class LazyLoadingConfigurer implements Configurer<LazyLoad> {

  @Override
  public Class<LazyLoad> getConfigClass() {
    return LazyLoad.class;
  }

  @Nonnull
  @Override
  public LazyLoad defaultConfig() {
    return LazyLoad.OFF;
  }

  @Nonnull
  @Override
  public LazyLoad getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      if (pkg.isAnnotationPresent(LazyLoadApplication.class)) {
        return pkg.getAnnotation(LazyLoadApplication.class).value();
      }
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  @Override
  public LazyLoad getConfigFor(@Nonnull Class<?> testClass) {
    if (testClass.isAnnotationPresent(LazyLoadApplication.class)) {
      return testClass.getAnnotation(LazyLoadApplication.class).value();
    } else {
      return null;
    }
  }

  @Override
  public LazyLoad getConfigFor(@Nonnull Method method) {
    if (method.isAnnotationPresent(LazyLoadApplication.class)) {
      return method.getAnnotation(LazyLoadApplication.class).value();
    } else {
      return null;
    }
  }

  /** "Merges" two configurations together. Child configuration always overrides the parent */
  @Nonnull
  @Override
  public LazyLoad merge(@Nonnull LazyLoad parentConfig, @Nonnull LazyLoad childConfig) {
    return childConfig;
  }
}

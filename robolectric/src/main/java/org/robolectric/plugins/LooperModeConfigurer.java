package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.pluginapi.config.Configurer;

/** Provides configuration to Robolectric for its &#064;{@link LooperMode} annotation. */
@AutoService(Configurer.class)
public class LooperModeConfigurer implements Configurer<LooperMode.Mode> {

  private Properties systemProperties;

  public LooperModeConfigurer(Properties systemProperties) {
    this.systemProperties = systemProperties;
  }

  @Override
  public Class<LooperMode.Mode> getConfigClass() {
    return LooperMode.Mode.class;
  }

  @Nonnull
  @Override
  public LooperMode.Mode defaultConfig() {
    return LooperMode.Mode.valueOf(
        systemProperties.getProperty("robolectric.looperMode", "PAUSED"));
  }

  @Override
  public LooperMode.Mode getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      return valueFrom(pkg.getAnnotation(LooperMode.class));
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  @Override
  public LooperMode.Mode getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(LooperMode.class));
  }

  @Override
  public LooperMode.Mode getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(LooperMode.class));
  }

  @Nonnull
  @Override
  public LooperMode.Mode merge(
      @Nonnull LooperMode.Mode parentConfig, @Nonnull LooperMode.Mode childConfig) {
    // just take the childConfig - since LooperMode only has a single 'value'
    // attribute
    return childConfig;
  }

  private Mode valueFrom(LooperMode looperMode) {
    return looperMode == null ? null : looperMode.value();
  }
}

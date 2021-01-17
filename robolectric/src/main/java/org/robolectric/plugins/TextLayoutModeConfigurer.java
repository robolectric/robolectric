package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.annotation.TextLayoutMode.Mode;
import org.robolectric.pluginapi.config.Configurer;

/** Provides configuration to Robolectric for its &#064;{@link TextLayoutMode} annotation. */
@AutoService(Configurer.class)
public class TextLayoutModeConfigurer implements Configurer<TextLayoutMode.Mode> {

  @Override
  public Class<TextLayoutMode.Mode> getConfigClass() {
    return TextLayoutMode.Mode.class;
  }

  @Nonnull
  @Override
  public TextLayoutMode.Mode defaultConfig() {
    return TextLayoutMode.Mode.REALISTIC;
  }

  @Override
  public TextLayoutMode.Mode getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      return valueFrom(pkg.getAnnotation(TextLayoutMode.class));
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  @Override
  public TextLayoutMode.Mode getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(TextLayoutMode.class));
  }

  @Override
  public TextLayoutMode.Mode getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(TextLayoutMode.class));
  }

  @Nonnull
  @Override
  public TextLayoutMode.Mode merge(
      @Nonnull TextLayoutMode.Mode parentConfig, @Nonnull TextLayoutMode.Mode childConfig) {
    // just take the childConfig - since TextLayoutMode only has a single 'value'
    // attribute
    return childConfig;
  }

  private Mode valueFrom(TextLayoutMode looperMode) {
    return looperMode == null ? null : looperMode.value();
  }
}

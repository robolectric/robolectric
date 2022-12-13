package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.pluginapi.config.Configurer;

/** Provides configuration to Robolectric for its @{@link GraphicsMode} annotation. */
@AutoService(Configurer.class)
public class GraphicsModeConfigurer implements Configurer<GraphicsMode.Mode> {

  private final Properties systemProperties;

  public GraphicsModeConfigurer(Properties systemProperties) {
    this.systemProperties = systemProperties;
  }

  @Override
  public Class<GraphicsMode.Mode> getConfigClass() {
    return GraphicsMode.Mode.class;
  }

  @Nonnull
  @Override
  public GraphicsMode.Mode defaultConfig() {
    return GraphicsMode.Mode.valueOf(
        systemProperties.getProperty("robolectric.graphicsMode", "LEGACY"));
  }

  @Override
  public GraphicsMode.Mode getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      return valueFrom(pkg.getAnnotation(GraphicsMode.class));
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  @Override
  public GraphicsMode.Mode getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(GraphicsMode.class));
  }

  @Override
  public GraphicsMode.Mode getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(GraphicsMode.class));
  }

  @Nonnull
  @Override
  public GraphicsMode.Mode merge(
      @Nonnull GraphicsMode.Mode parentConfig, @Nonnull GraphicsMode.Mode childConfig) {
    // just take the childConfig - since GraphicsMode only has a single 'value' attribute
    return childConfig;
  }

  private Mode valueFrom(GraphicsMode graphicsMode) {
    return graphicsMode == null ? null : graphicsMode.value();
  }
}

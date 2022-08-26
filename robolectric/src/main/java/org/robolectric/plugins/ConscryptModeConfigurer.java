package org.robolectric.plugins;

import static com.google.common.base.StandardSystemProperty.OS_ARCH;
import static com.google.common.base.StandardSystemProperty.OS_NAME;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.ConscryptMode.Mode;
import org.robolectric.pluginapi.config.Configurer;

/** Provides configuration to Robolectric for its @{@link ConscryptMode} annotation. */
@AutoService(Configurer.class)
public class ConscryptModeConfigurer implements Configurer<ConscryptMode.Mode> {

  private final Properties systemProperties;

  public ConscryptModeConfigurer(Properties systemProperties) {
    this.systemProperties = systemProperties;
  }

  @Override
  public Class<ConscryptMode.Mode> getConfigClass() {
    return ConscryptMode.Mode.class;
  }

  @Nonnull
  @Override
  public ConscryptMode.Mode defaultConfig() {
    String defaultValue = "ON";
    String os = systemProperties.getProperty(OS_NAME.key(), "").toLowerCase(Locale.US);
    String arch = systemProperties.getProperty(OS_ARCH.key(), "").toLowerCase(Locale.US);
    if (os.contains("mac") && arch.equals("aarch64")) {
      defaultValue = "OFF";
    }
    return ConscryptMode.Mode.valueOf(
        systemProperties.getProperty("robolectric.conscryptMode", defaultValue));
  }

  @Override
  public ConscryptMode.Mode getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      return valueFrom(pkg.getAnnotation(ConscryptMode.class));
    } catch (ClassNotFoundException ignored) {
      // ignore
    }
    return null;
  }

  @Override
  public ConscryptMode.Mode getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(ConscryptMode.class));
  }

  @Override
  public ConscryptMode.Mode getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(ConscryptMode.class));
  }

  @Nonnull
  @Override
  public ConscryptMode.Mode merge(
      @Nonnull ConscryptMode.Mode parentConfig, @Nonnull ConscryptMode.Mode childConfig) {
    // just take the childConfig - since ConscryptMode only has a single 'value' attribute
    return childConfig;
  }

  private Mode valueFrom(ConscryptMode conscryptMode) {
    return conscryptMode == null ? null : conscryptMode.value();
  }
}

package org.robolectric.plugins;

import static com.google.common.base.StandardSystemProperty.OS_ARCH;
import static com.google.common.base.StandardSystemProperty.OS_NAME;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.SecurityMode;
import org.robolectric.annotation.SecurityMode.Mode;
import org.robolectric.pluginapi.config.Configurer;

/** Provides configuration to Robolectric for its @{@link SecurityMode} annotation. */
@AutoService(Configurer.class)
public class SecurityModeConfigurer implements Configurer<SecurityMode.Mode> {

  private final Properties systemProperties;

  public SecurityModeConfigurer(Properties systemProperties) {
    this.systemProperties = systemProperties;
  }

  @Override
  public Class<SecurityMode.Mode> getConfigClass() {
    return SecurityMode.Mode.class;
  }

  @Nonnull
  @Override
  public SecurityMode.Mode defaultConfig() {

    String defaultValue = "CONSCRYPT";

    String os = systemProperties.getProperty(OS_NAME.key(), "").toLowerCase(Locale.US);
    String arch = systemProperties.getProperty(OS_ARCH.key(), "").toLowerCase(Locale.US);
    if (os.contains("mac") && arch.equals("aarch64")) {

      defaultValue = "CONSCRYPT";
    }
    return SecurityMode.Mode.valueOf(
        systemProperties.getProperty("robolectric.securityMode", defaultValue));
  }

  @Override
  public SecurityMode.Mode getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      return valueFrom(pkg.getAnnotation(SecurityMode.class));
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  @Override
  public SecurityMode.Mode getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(SecurityMode.class));
  }

  @Override
  public SecurityMode.Mode getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(SecurityMode.class));
  }

  @Nonnull
  @Override
  public SecurityMode.Mode merge(
      @Nonnull SecurityMode.Mode parentConfig, @Nonnull SecurityMode.Mode childConfig) {
    // just take the childConfig - since SecurityMode only has a single 'value' attribute
    return childConfig;
  }

  private Mode valueFrom(SecurityMode securityMode) {
    return securityMode == null ? null : securityMode.value();
  }
}

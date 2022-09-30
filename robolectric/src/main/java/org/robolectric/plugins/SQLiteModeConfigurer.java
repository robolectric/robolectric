package org.robolectric.plugins;

import static com.google.common.base.StandardSystemProperty.OS_NAME;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;
import org.robolectric.pluginapi.config.Configurer;

/** Provides configuration to Robolectric for its @{@link SQLiteMode} annotation. */
@AutoService(Configurer.class)
public class SQLiteModeConfigurer implements Configurer<SQLiteMode.Mode> {

  private final Properties systemProperties;

  public SQLiteModeConfigurer(Properties systemProperties) {
    this.systemProperties = systemProperties;
  }

  @Override
  public Class<SQLiteMode.Mode> getConfigClass() {
    return SQLiteMode.Mode.class;
  }

  @Nonnull
  @Override
  public SQLiteMode.Mode defaultConfig() {
    String defaultValue = "NATIVE";
    String os = systemProperties.getProperty(OS_NAME.key(), "").toLowerCase(Locale.US);
    // NATIVE SQLite mode not supported on Windows
    if (os.contains("win")) {
      defaultValue = "LEGACY";
    }
    return SQLiteMode.Mode.valueOf(
        systemProperties.getProperty("robolectric.sqliteMode", defaultValue));
  }

  @Override
  public SQLiteMode.Mode getConfigFor(@Nonnull String packageName) {
    try {
      Package pkg = Class.forName(packageName + ".package-info").getPackage();
      return valueFrom(pkg.getAnnotation(SQLiteMode.class));
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  @Override
  public SQLiteMode.Mode getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(SQLiteMode.class));
  }

  @Override
  public SQLiteMode.Mode getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(SQLiteMode.class));
  }

  @Nonnull
  @Override
  public SQLiteMode.Mode merge(
      @Nonnull SQLiteMode.Mode parentConfig, @Nonnull SQLiteMode.Mode childConfig) {
    // just take the childConfig - since SQLiteMode only has a single 'value' attribute
    return childConfig;
  }

  private Mode valueFrom(SQLiteMode sqliteMode) {
    return sqliteMode == null ? null : sqliteMode.value();
  }
}

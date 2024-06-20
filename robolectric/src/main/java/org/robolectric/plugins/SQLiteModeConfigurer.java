package org.robolectric.plugins;

import static com.google.common.base.StandardSystemProperty.OS_NAME;

import com.google.auto.service.AutoService;
import java.util.Locale;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;

/** Provides configuration to Robolectric for its @{@link SQLiteMode} annotation. */
@AutoService(Configurer.class)
public class SQLiteModeConfigurer extends SingleValueConfigurer<SQLiteMode, SQLiteMode.Mode> {

  public SQLiteModeConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(
        SQLiteMode.class,
        SQLiteMode.Mode.class,
        defaultValue(systemProperties),
        propertyFileLoader,
        systemProperties);
  }

  @Override
  protected String propertyName() {
    return "sqliteMode";
  }

  @Nonnull
  private static final SQLiteMode.Mode defaultValue(Properties properties) {
    String os = properties.getProperty(OS_NAME.key(), "").toLowerCase(Locale.US);
    // NATIVE SQLite mode not supported on Windows
    if (os.contains("win")) {
      return Mode.LEGACY;
    }
    return Mode.NATIVE;
  }
}

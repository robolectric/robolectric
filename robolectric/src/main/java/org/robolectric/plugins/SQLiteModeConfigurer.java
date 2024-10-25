package org.robolectric.plugins;


import com.google.auto.service.AutoService;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;
import org.robolectric.util.OsUtil;

/** Provides configuration to Robolectric for its @{@link SQLiteMode} annotation. */
@AutoService(Configurer.class)
public class SQLiteModeConfigurer extends SingleValueConfigurer<SQLiteMode, SQLiteMode.Mode> {

  public SQLiteModeConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(
        SQLiteMode.class,
        SQLiteMode.Mode.class,
        defaultValue(),
        propertyFileLoader,
        systemProperties);
  }

  @Override
  protected String propertyName() {
    return "sqliteMode";
  }

  @Nonnull
  private static final SQLiteMode.Mode defaultValue() {
    // NATIVE SQLite mode not supported on Windows
    if (OsUtil.isWindows()) {
      return Mode.LEGACY;
    }
    return Mode.NATIVE;
  }
}

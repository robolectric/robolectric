package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.util.Properties;
import org.robolectric.annotation.SQLiteMode;
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
        SQLiteMode.Mode.NATIVE,
        propertyFileLoader,
        systemProperties);
  }

  @Override
  protected String propertyName() {
    return "sqliteMode";
  }
}

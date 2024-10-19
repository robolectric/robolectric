package org.robolectric.plugins;

import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static com.google.common.truth.Truth.assertThat;

import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;

/** Unit tests for methods annotated with @{@link SQLiteMode}. */
@RunWith(JUnit4.class)
public class SQLiteModeConfigurerTest {

  @Test
  public void defaultConfigWithPrePopulatedSQLiteMode() {
    Properties systemProperties1 = new Properties();
    SQLiteModeConfigurer configurer1 =
        new SQLiteModeConfigurer(systemProperties1, new PackagePropertiesLoader());

    systemProperties1.setProperty("robolectric.sqliteMode", "LEGACY");
    assertThat(configurer1.defaultConfig()).isSameInstanceAs(Mode.LEGACY);

    Properties systemProperties2 = new Properties();
    SQLiteModeConfigurer configurer2 =
        new SQLiteModeConfigurer(systemProperties2, new PackagePropertiesLoader());
    systemProperties2.setProperty("robolectric.sqliteMode", "NATIVE");
    assertThat(configurer2.defaultConfig()).isSameInstanceAs(Mode.NATIVE);
  }

  @Test
  public void osArchSpecificConfig() {
    String oldName = OS_NAME.value();
    try {
      System.setProperty("os.name", "Mac OS X");
      SQLiteModeConfigurer configurer1 =
          new SQLiteModeConfigurer(System.getProperties(), new PackagePropertiesLoader());
      assertThat(configurer1.defaultConfig()).isSameInstanceAs(Mode.NATIVE);

      System.setProperty("os.name", "Windows 7");
      SQLiteModeConfigurer configurer2 =
          new SQLiteModeConfigurer(System.getProperties(), new PackagePropertiesLoader());

      assertThat(configurer2.defaultConfig()).isSameInstanceAs(Mode.LEGACY);
    } finally {
      System.setProperty("os.name", oldName);
    }
  }
}

package org.robolectric.plugins;

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
  public void defaultConfig() {
    Properties systemProperties = new Properties();
    SQLiteModeConfigurer configurer = new SQLiteModeConfigurer(systemProperties);
    assertThat(configurer.defaultConfig()).isSameInstanceAs(Mode.LEGACY);

    systemProperties.setProperty("robolectric.sqliteMode", "LEGACY");
    assertThat(configurer.defaultConfig()).isSameInstanceAs(Mode.LEGACY);

    systemProperties.setProperty("robolectric.sqliteMode", "NATIVE");
    assertThat(configurer.defaultConfig()).isSameInstanceAs(Mode.NATIVE);
  }
}

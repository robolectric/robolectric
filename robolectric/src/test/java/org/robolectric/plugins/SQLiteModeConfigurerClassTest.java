package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;

import android.database.CursorWindow;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowCursorWindow;
import org.robolectric.shadows.ShadowLegacyCursorWindow;
import org.robolectric.shadows.ShadowNativeCursorWindow;

/** Unit tests for classes annotated with @LooperMode. */
@RunWith(AndroidJUnit4.class)
@SQLiteMode(Mode.LEGACY)
public class SQLiteModeConfigurerClassTest {

  @Test
  public void defaultsToClass() {
    assertThat(ConfigurationRegistry.get(SQLiteMode.Mode.class)).isSameInstanceAs(Mode.LEGACY);
  }

  @Test
  @SQLiteMode(Mode.NATIVE)
  public void overriddenAtMethod() {
    assertThat(ConfigurationRegistry.get(Mode.class)).isSameInstanceAs(Mode.NATIVE);
  }

  @Test
  @SQLiteMode(Mode.LEGACY)
  public void shouldUseLegacyShadows() {
    assertThat(ConfigurationRegistry.get(Mode.class)).isSameInstanceAs(Mode.LEGACY);
    try (CursorWindow cursorWindow = new CursorWindow("1")) {
      ShadowCursorWindow shadow = Shadow.extract(cursorWindow);
      assertThat(shadow).isInstanceOf(ShadowLegacyCursorWindow.class);
    }
  }

  @Test
  @SQLiteMode(Mode.NATIVE)
  public void shouldUseRealisticShadows() {
    assertThat(ConfigurationRegistry.get(Mode.class)).isSameInstanceAs(Mode.NATIVE);
    try (CursorWindow cursorWindow = new CursorWindow("2")) {
      ShadowCursorWindow shadow = Shadow.extract(cursorWindow);
      assertThat(shadow).isInstanceOf(ShadowNativeCursorWindow.class);
    }
  }

  @Test
  @SQLiteMode(Mode.NATIVE)
  @Config(shadows = MyShadowCursorWindow.class)
  public void shouldPreferCustomShadows() {
    assertThat(ConfigurationRegistry.get(Mode.class)).isSameInstanceAs(Mode.NATIVE);
    try (CursorWindow cursorWindow = new CursorWindow("3")) {
      ShadowCursorWindow shadow = Shadow.extract(cursorWindow);
      assertThat(shadow).isInstanceOf(MyShadowCursorWindow.class);
    }
  }

  /** A custom {@link android.database.CursorWindow} shadow for testing */
  @Implements(CursorWindow.class)
  public static class MyShadowCursorWindow extends ShadowLegacyCursorWindow {}
}

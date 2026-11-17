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
import org.robolectric.shadows.ShadowNativeCursorWindow;

/** Unit tests for the SQLite mode of tests, which is always native. */
@RunWith(AndroidJUnit4.class)
public class SQLiteModeConfigurerClassTest {

  @Test
  public void defaultsToNative() {
    assertThat(ConfigurationRegistry.get(SQLiteMode.Mode.class)).isSameInstanceAs(Mode.NATIVE);
  }

  @Test
  public void shouldUseNativeShadows() {
    try (CursorWindow cursorWindow = new CursorWindow("1")) {
      ShadowNativeCursorWindow shadow = Shadow.extract(cursorWindow);
      assertThat(shadow).isInstanceOf(ShadowNativeCursorWindow.class);
    }
  }

  @Test
  @Config(shadows = MyShadowCursorWindow.class)
  public void shouldPreferCustomShadows() {
    try (CursorWindow cursorWindow = new CursorWindow("2")) {
      ShadowNativeCursorWindow shadow = Shadow.extract(cursorWindow);
      assertThat(shadow).isInstanceOf(MyShadowCursorWindow.class);
    }
  }

  /** A custom {@link android.database.CursorWindow} shadow for testing */
  @Implements(value = CursorWindow.class, callNativeMethodsByDefault = true)
  public static class MyShadowCursorWindow extends ShadowNativeCursorWindow {}
}

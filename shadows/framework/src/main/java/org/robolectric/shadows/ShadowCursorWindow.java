package org.robolectric.shadows;

import android.database.CursorWindow;
import org.robolectric.annotation.Implements;

/**
 * The base shadow class for {@link CursorWindow}.
 *
 * <p>The actual shadow class for {@link CursorWindow} will be selected during runtime by the
 * Picker.
 */
@Implements(value = CursorWindow.class, shadowPicker = ShadowCursorWindow.Picker.class)
public class ShadowCursorWindow {
  /** Shadow {@link Picker} for {@link ShadowCursorWindow} */
  public static class Picker extends SQLiteShadowPicker<ShadowCursorWindow> {
    public Picker() {
      super(ShadowLegacyCursorWindow.class, ShadowNativeCursorWindow.class);
    }
  }
}

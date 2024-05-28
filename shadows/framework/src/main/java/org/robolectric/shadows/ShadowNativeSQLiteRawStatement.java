package org.robolectric.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link android.database.sqlite.SQLiteRawStatement}. */
@SuppressWarnings(
    "robolectric.internal.IgnoreMissingClass") // Remove when Robolectric compiles against V
@Implements(
    className = "android.database.sqlite.SQLiteRawStatement",
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true,
    minSdk = V.SDK_INT,
    shadowPicker = ShadowNativeSQLiteRawStatement.Picker.class)
public class ShadowNativeSQLiteRawStatement {

  /** Shadow {@link Picker} for {@link ShadowCursorWindow} */
  public static class Picker extends SQLiteShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeSQLiteRawStatement.class);
    }
  }
}

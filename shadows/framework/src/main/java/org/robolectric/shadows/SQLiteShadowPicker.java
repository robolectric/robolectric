package org.robolectric.shadows;

import com.google.common.collect.ImmutableSet;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.ShadowPicker;

/** A {@link ShadowPicker} that selects between shadows given the SQLite mode */
public class SQLiteShadowPicker<T> implements ShadowPicker<T> {

  private final Class<? extends T> legacyShadowClass;
  private final Class<? extends T> nativeShadowClass;

  private static final ImmutableSet<String> AFFECTED_CLASSES =
      ImmutableSet.of("android.database.CursorWindow", "android.database.sqlite.SQLiteConnection");

  public SQLiteShadowPicker(
      Class<? extends T> legacyShadowClass, Class<? extends T> nativeShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.nativeShadowClass = nativeShadowClass;
  }

  @Override
  public Class<? extends T> pickShadowClass() {
    if (ConfigurationRegistry.get(SQLiteMode.Mode.class) == Mode.NATIVE) {
      return nativeShadowClass;
    } else {
      return legacyShadowClass;
    }
  }

  /**
   * Returns a list of shadow classes that need to be invalidated when the SQLite Mode is switched.
   */
  public static ImmutableSet<String> getAffectedClasses() {
    return AFFECTED_CLASSES;
  }
}

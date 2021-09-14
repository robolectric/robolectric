package org.robolectric.shadows;

import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.ShadowPicker;

/** A {@link ShadowPicker} that selects between shadows given the SQLite mode */
public class SQLiteShadowPicker<T> implements ShadowPicker<T> {

  private final Class<? extends T> legacyShadowClass;
  private final Class<? extends T> nativeShadowClass;

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
}

package org.robolectric.shadows;

import android.view.KeyCharacterMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow for {@link android.view.KeyCharacterMap}.
 */
@Implements(KeyCharacterMap.class)
public class ShadowKeyCharacterMap {
  @Implementation
  public static KeyCharacterMap load(int deviceId) {
    return ReflectionHelpers.callConstructor(KeyCharacterMap.class);
  }
}

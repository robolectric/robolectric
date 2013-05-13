package org.robolectric.shadows;

import android.view.KeyCharacterMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.robolectric.Robolectric.newInstanceOf;

@Implements(KeyCharacterMap.class)
public class ShadowKeyCharacterMap {
  @Implementation
  public static KeyCharacterMap load(int deviceId) {
    return newInstanceOf(KeyCharacterMap.class);
  }
}

package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link KeyEvent}. */
@Implements(KeyEvent.class)
public class ShadowKeyEvent extends ShadowInputEvent {

  private static final Map<String, Integer> keyCodeMap;

  static {
    keyCodeMap = new HashMap<>();
    String keyCodeConstantPrefix = reflector(KeyEventReflector.class).getLabelPrefix();
    for (Field field : KeyEvent.class.getDeclaredFields()) {
      if (field.getName().startsWith(keyCodeConstantPrefix)
          && Modifier.isStatic(field.getModifiers())) {
        String keyCodeString = field.getName().substring(keyCodeConstantPrefix.length());
        try {
          keyCodeMap.put(keyCodeString, field.getInt(null));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(
              "unable to get reflectively get value for " + keyCodeString, e);
        }
      }
    }
  }

  @Implementation
  protected static int nativeKeyCodeFromString(String keyCode) {
    return keyCodeMap.getOrDefault(keyCode, KeyEvent.KEYCODE_UNKNOWN);
  }

  @ForType(KeyEvent.class)
  private interface KeyEventReflector {

    @Accessor("LABEL_PREFIX")
    String getLabelPrefix();
  }
}

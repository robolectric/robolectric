package org.robolectric.shadows;

import android.content.ContentResolver;
import com.google.android.gsf.Gservices;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link Gservices} that allows simple overrides for testing. */
@Implements(Gservices.class)
public class ShadowGservices {
  private static final Map<String, String> overrideMap = new HashMap<>();

  @Implementation
  public static String getString(
      @SuppressWarnings("unused") ContentResolver cr, String key, String defValue) {
    String value = overrideMap.get(key);
    return value != null ? value : defValue;
  }

  @Implementation
  public static Long getLong(
      @SuppressWarnings("unused") ContentResolver cr, String key, long defValue) {
    String value = overrideMap.get(key);
    return value != null ? Long.parseLong(value) : defValue;
  }

  @Implementation
  public static boolean getBoolean(
      @SuppressWarnings("unused") ContentResolver cr, String key, boolean defValue) {
    String value = overrideMap.get(key);
    if (value == null) {
      return defValue;
    } else if (Gservices.TRUE_PATTERN.matcher(value).matches()) {
      return true;
    } else if (Gservices.FALSE_PATTERN.matcher(value).matches()) {
      return false;
    } else {
      return defValue;
    }
  }

  @Implementation
  public static Map<String, String> getStringsByPrefix(
      @SuppressWarnings("unused") ContentResolver cr, String... prefixes) {
    Map<String, String> strings = new HashMap<>();
    for (String key : overrideMap.keySet()) {
      for (String prefix : prefixes) {
        if (key.startsWith(prefix)) {
          strings.put(key, overrideMap.get(key));
        }
      }
    }
    return strings;
  }

  /** Overrides the GservicesValue based on the key with the given value. */
  public static <T> void override(String value, T override) {
    overrideMap.put(value, override.toString());
  }

  /** Clear all overrides set previously. Recommended to do this before all tests. */
  public static void resetOverrides() {
    overrideMap.clear();
  }
}

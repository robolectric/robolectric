package org.robolectric.annotation.internal;

import android.os.Build;
import org.jspecify.annotations.NonNull;
import org.robolectric.annotation.Config;

public class ConfigUtils {
  private ConfigUtils() {}

  public static @NonNull String @NonNull [] parseStringArrayProperty(@NonNull String property) {
    if (property.isEmpty()) return new String[0];
    return property.split("[, ]+");
  }

  public static int @NonNull [] parseSdkArrayProperty(@NonNull String property) {
    String[] parts = parseStringArrayProperty(property);
    int[] result = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      result[i] = parseSdkInt(parts[i]);
    }

    return result;
  }

  public static int parseSdkInt(@NonNull String part) {
    String spec = part.trim();
    switch (spec) {
      case "ALL_SDKS":
        return Config.ALL_SDKS;
      case "TARGET_SDK":
        return Config.TARGET_SDK;
      case "OLDEST_SDK":
        return Config.OLDEST_SDK;
      case "NEWEST_SDK":
        return Config.NEWEST_SDK;
      default:
        try {
          return Integer.parseInt(spec);
        } catch (NumberFormatException e) {
          try {
            return (int) Build.VERSION_CODES.class.getField(part).get(null);
          } catch (Exception e2) {
            throw new IllegalArgumentException("unknown SDK \"" + part + "\"");
          }
        }
    }
  }
}

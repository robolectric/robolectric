package org.robolectric.shadows;

import java.lang.reflect.Field;
import java.util.regex.Pattern;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Implements OsConstants on APIs 20 and below. */
@Implements(className = "libcore.io.OsConstants", maxSdk = 20, isInAndroidSdk = false)
public final class ShadowLibcoreOsConstants {
  private static final Pattern ERRNO_PATTERN = Pattern.compile("E[A-Z0-9]+");

  @Implementation
  protected static void initConstants() {
    int errnos = 1;
    try {
      for (Field field : Class.forName("libcore.io.OsConstants").getDeclaredFields()) {
        if (ERRNO_PATTERN.matcher(field.getName()).matches() && field.getType() == int.class) {
          field.setInt(null, errnos++);
        }

        // Type of file.
        if (field.getName().equals(OsConstantsValues.S_IFMT)) {
          field.setInt(null, OsConstantsValues.S_IFMT_VALUE);
          continue;
        }
        // Directory.
        if (field.getName().equals(OsConstantsValues.S_IFDIR)) {
          field.setInt(null, OsConstantsValues.S_IFDIR_VALUE);
          continue;
        }
        // Regular file.
        if (field.getName().equals(OsConstantsValues.S_IFREG)) {
          field.setInt(null, OsConstantsValues.S_IFREG_VALUE);
          continue;
        }
        // Symbolic link.
        if (field.getName().equals(OsConstantsValues.S_IFLNK)) {
          field.setInt(null, OsConstantsValues.S_IFLNK_VALUE);
        }
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}

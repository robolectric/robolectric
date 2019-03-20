package org.robolectric.shadows;

import android.system.OsConstants;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** */
@Implements(value = OsConstants.class, minSdk = 21)
public final class ShadowOsConstants {
  private static final Pattern ERRNO_PATTERN = Pattern.compile("E[A-Z0-9]+");

  @Implementation
  protected static void initConstants() {
    int errnos = 1;
    try {
      for (Field field : OsConstants.class.getDeclaredFields()) {
        if (ERRNO_PATTERN.matcher(field.getName()).matches() && field.getType() == int.class) {
          field.setInt(null, errnos++);
        }
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}

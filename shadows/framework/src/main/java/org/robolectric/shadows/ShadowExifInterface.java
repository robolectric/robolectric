package org.robolectric.shadows;

import android.media.ExifInterface;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link ExifInterface}. */
@Implements(ExifInterface.class)
public class ShadowExifInterface {
  private static final Map<String, String> attributes = new HashMap<>();

  @Implementation
  public int getAttributeInt(String tag, int defaultValue) {
    String exifAttribute = attributes.get(tag);
    if (exifAttribute == null) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(exifAttribute);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Sets an attribute on the {@link ExifInterface}.
   *
   * <p>Note: Call {@link #reset()} between tests to remove any previously added attributes.
   */
  public static void setAttribute(String tag, String value) {
    attributes.put(tag, value);
  }

  /** Reset the state of the {@link ShadowExifInterface}. */
  public static void reset() {
    attributes.clear();
  }
}

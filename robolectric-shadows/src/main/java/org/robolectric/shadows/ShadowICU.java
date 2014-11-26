package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(libcore.icu.ICU.class)
public class ShadowICU {

  @Implementation
  public static String addLikelySubtags(String locale) {
    return "en-US";
  }
}

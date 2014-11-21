package org.robolectric.shadows;

import android.net.Uri;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Implements(Uri.class)
public class ShadowUri {
  /**
   * Avoid a reference to Android's custom java.nio.charset.Charsets.
   */
  @Implementation
  public static String decode(String s) {
    if (s == null) {
      return null;
    }
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

}

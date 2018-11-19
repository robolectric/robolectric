package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import libcore.timezone.TimeZoneFinder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = TimeZoneFinder.class, minSdk = Q, isInAndroidSdk = false, looseSignatures = true)
public class ShadowTimeZoneFinder {

  private static final String TZLOOKUP_PATH = "/usr/share/zoneinfo/tzlookup.xml";

  @Implementation
  protected static Object getInstance() {
    return TimeZoneFinder.createInstanceForTests(readTzlookup());
  }

  /**
   * Reads tzlookup.xml from the files bundled inside android-all JARs. We need to read the file
   * instead of passing in the path because the real implementation uses {@link java.nio.file.Paths}
   * which doesn't support reading from JARs.
   */
  private static String readTzlookup() {
    StringBuilder stringBuilder = new StringBuilder();
    InputStream is = null;
    try {
      try {
        is = ShadowTimeZoneFinder.class.getResourceAsStream(TZLOOKUP_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        for (String line; (line = reader.readLine()) != null; ) {
          stringBuilder.append(line);
        }
      } finally {
        if (is != null) {
          is.close();
        }
      }
    } catch (IOException e) {
      // ignore
    }

    return stringBuilder.toString();
  }
}

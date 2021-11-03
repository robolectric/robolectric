package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.android.i18n.timezone.TimeZoneFinder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for TimeZoneFinder on S or above. */
@Implements(
    value = TimeZoneFinder.class,
    minSdk = S,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowTimeZoneFinderS {

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
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8));
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

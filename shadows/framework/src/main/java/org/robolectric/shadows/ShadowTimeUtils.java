package org.robolectric.shadows;

import android.util.TimeUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link TimeUtils}. */
@Implements(value = TimeUtils.class)
public class ShadowTimeUtils {
  // BEGIN-NTERNAL
  public static Map<String, List<String>> timeZoneIdsMap = new HashMap<>();

  public static void setTimeZoneIdsForCountryCode(String countryCode, String[] timeZoneIds) {
    timeZoneIdsMap.put(countryCode.toLowerCase(), Arrays.asList(timeZoneIds));
  }

  @Resetter
  public static void reset() {
    timeZoneIdsMap.clear();
  }
  /**
   * Returns time zone IDs for time zones known to be associated with a country.
   *
   * <p>The list returned may be different from other on-device sources like {@link
   * android.icu.util.TimeZone#getRegion(String)} as it can be curated to avoid contentious
   * mappings.
   *
   * @param countryCode the ISO 3166-1 alpha-2 code for the country as can be obtained using {@link
   *     java.util.Locale#getCountry()}
   * @return IDs that can be passed to {@link java.util.TimeZone#getTimeZone(String)} or similar
   *     methods, or {@code null} if the countryCode is unrecognized
   */
  @Implementation(minSdk = android.os.Build.VERSION_CODES.Q)
  public static List<String> getTimeZoneIdsForCountryCode(String countryCode) {
    if (countryCode == null) {
      throw new NullPointerException("countryCode == null");
    }

    List<String> timeZoneIds = timeZoneIdsMap.get(countryCode.toLowerCase());
    if (timeZoneIds == null) {
      return null;
    } else {
      return Collections.unmodifiableList(timeZoneIds);
    }
  }
  // END-INTERNAL
}

package org.robolectric.shadows;

import java.text.FieldPosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import libcore.icu.DateIntervalFormat;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;

import static android.os.Build.VERSION_CODES.KITKAT;

@Implements(value = DateIntervalFormat.class, isInAndroidSdk = false, minSdk = KITKAT)
public class ShadowDateIntervalFormat {

  private static long address;
  private static Map<Long, com.ibm.icu.text.DateIntervalFormat> INTERVAL_CACHE = new HashMap<>();

  @Implementation
  public static long createDateIntervalFormat(String skeleton, String localeName, String tzName) {
    address++;
    INTERVAL_CACHE.put(address, com.ibm.icu.text.DateIntervalFormat.getInstance(skeleton, new Locale(localeName)));
    return address;
  }

  @Implementation
  public static void destroyDateIntervalFormat(long address) {
    INTERVAL_CACHE.remove(address);
  }

  @Implementation
  public static String formatDateInterval(long address, long fromDate, long toDate) {
    StringBuffer buffer = new StringBuffer();

    FieldPosition pos = new FieldPosition(0);
    INTERVAL_CACHE.get(address).format(new com.ibm.icu.util.DateInterval(fromDate, toDate), buffer, pos);

    return buffer.toString();
  }
}

package org.robolectric.util;

import android.location.Location;
import com.google.common.base.Objects;

public class LocationUtils {
  /**
   * Compares two {@link Location} objects for equality.
   */
  public static boolean locationEquals(Location l1, Location l2) {
    return l1 == l2 || l1 != null && l2 != null &&
        Double.compare(l1.getLatitude(), l2.getLatitude()) == 0 &&
        Double.compare(l2.getLongitude(), l2.getLongitude()) == 0 &&
        l1.getTime() == l2.getTime() &&
        Float.compare(l1.getAccuracy(), l2.getAccuracy()) == 0 &&
        Objects.equal(l1.getProvider(), l2.getProvider());
  }
}

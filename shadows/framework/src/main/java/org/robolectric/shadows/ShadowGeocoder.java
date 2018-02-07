package org.robolectric.shadows;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link Geocoder}. */
@Implements(Geocoder.class)
public final class ShadowGeocoder {
  private static boolean isPresent = true;
  private List<Address> fromLocation = new ArrayList<>();

  /** Returns true by default, or the last value set by {@link #setIsPresent(boolean)}. */
  @Implementation
  public static boolean isPresent() {
    return isPresent;
  }

  /**
   * Returns an empty list by default, or the last value set by {@link #setFromLocation(List)}
   * @param latitude it's ignored by this implementation
   * @param longitude it's ignored by this implementation
   * @param maxResults max number of addresses to return
   * @throws IOException - never thrown, only keeping the same signature
   */
  @Implementation
  public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
      return fromLocation.subList(0, Math.min(maxResults, fromLocation.size()));
  }

  /** Statically sets the value to be returned by {@link #isPresent()}. */
  public static void setIsPresent(boolean value) {
    isPresent = value;
  }

  /** Sets the value to be returned by {@link #getFromLocation(double, double, int)} */
  public void setFromLocation(List<Address> list) { fromLocation = list; }

  @Resetter
  public static void reset() {
    isPresent = true;
  }
}

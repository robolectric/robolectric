package org.robolectric.shadows;

import android.location.Geocoder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link Geocoder}. */
@Implements(Geocoder.class)
public final class ShadowGeocoder {
  private static boolean isPresent = true;

  /** Returns true by default, or the last value set by {@link #setIsPresent(boolean)}. */
  @Implementation
  public static boolean isPresent() {
    return isPresent;
  }

  /** Statically sets the value to be returned by {@link #isPresent()}. */
  public static void setIsPresent(boolean value) {
    isPresent = value;
  }

  @Resetter
  public static void reset() {
    isPresent = true;
  }
}

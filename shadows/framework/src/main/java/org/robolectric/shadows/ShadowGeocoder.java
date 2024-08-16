package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.location.Address;
import android.location.Geocoder;
import android.location.Geocoder.GeocodeListener;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(Geocoder.class)
public final class ShadowGeocoder {

  private static boolean isPresent = true;
  private List<Address> fromLocation = new ArrayList<>();
  private String errorMessage = null;

  /**
   * @return true by default, or the value specified via {@link #setIsPresent(boolean)}
   */
  @Implementation
  protected static boolean isPresent() {
    return isPresent;
  }

  /**
   * Returns an empty list by default, or the last value set by {@link #setFromLocation(List)}
   *
   * <p>{@param latitude} and {@param longitude} are ignored by this implementation, except to check
   * that they are in appropriate bounds. {@param maxResults} determines the maximum number of
   * addresses to return.
   */
  @Implementation
  protected List<Address> getFromLocation(double latitude, double longitude, int maxResults)
      throws IOException {
    Preconditions.checkArgument(
        -90 <= latitude && latitude <= 90, "Latitude must be between -90 and 90, got %s", latitude);
    Preconditions.checkArgument(
        -180 <= longitude && longitude <= 180,
        "Longitude must be between -180 and 180, got %s",
        longitude);
    return fromLocation.subList(0, Math.min(maxResults, fromLocation.size()));
  }

  /**
   * Sets an empty list by default, or the last value set by {@link #setFromLocation(List)} in the
   * provided {@code listener}
   *
   * <p>{@code latitude} and {@code longitude} are ignored by this implementation, except to check
   * that they are in appropriate bounds. {@code maxResults} determines the maximum number of
   * addresses to return.
   */
  @Implementation(minSdk = TIRAMISU)
  protected void getFromLocation(
      double latitude, double longitude, int maxResults, GeocodeListener listener)
      throws IOException {
    Preconditions.checkArgument(
        -90 <= latitude && latitude <= 90, "Latitude must be between -90 and 90, got %s", latitude);
    Preconditions.checkArgument(
        -180 <= longitude && longitude <= 180,
        "Longitude must be between -180 and 180, got %s",
        longitude);

    // On real Android this callback will not happen synchronously.
    if (errorMessage != null) {
      listener.onError(errorMessage);
    } else {
      listener.onGeocode(fromLocation.subList(0, Math.min(maxResults, fromLocation.size())));
    }
  }

  /**
   * Sets an empty list by default, or the last value set by {@link #setFromLocation(List)} in the
   * provided {@code listener}
   *
   * <p>{@code locationName} is ignored by this implementation. {@code maxResults} determines the
   * maximum number of addresses to return.
   */
  @Implementation(minSdk = TIRAMISU)
  protected void getFromLocationName(
      String locationName, int maxResults, GeocodeListener listener) {
    if (errorMessage != null) {
      listener.onError(errorMessage);
    } else {
      listener.onGeocode(fromLocation.subList(0, Math.min(maxResults, fromLocation.size())));
    }
  }

  /**
   * Sets the value to be returned by {@link Geocoder#isPresent()}.
   *
   * <p>This value is reset to true for each test.
   */
  public static void setIsPresent(boolean value) {
    isPresent = value;
  }

  /** Sets the value to be returned by {@link Geocoder#getFromLocation(double, double, int)}. */
  public void setFromLocation(List<Address> list) {
    fromLocation = list;
  }

  /** Sets the value to be passed to {@link GeocodeListener#onError(String)}. */
  public void setErrorMessage(@Nullable String message) {
    errorMessage = message;
  }

  @Resetter
  public static void reset() {
    isPresent = true;
  }
}

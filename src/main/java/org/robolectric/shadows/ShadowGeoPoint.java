package org.robolectric.shadows;

import com.google.android.maps.GeoPoint;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.robolectric.Robolectric.shadowOf_;
import static org.robolectric.shadows.ShadowMapView.fromE6;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GeoPoint.class)
public class ShadowGeoPoint {
  private int lat;
  private int lng;

  public void __constructor__(int lat, int lng) {
    this.lat = lat;
    this.lng = lng;
  }

  @Implementation
  public int getLatitudeE6() {
    return lat;
  }

  @Implementation
  public int getLongitudeE6() {
    return lng;
  }

  @Override @Implementation
  public boolean equals(Object o) {
    if (o == null) return false;
    o = shadowOf_(o);
    if (o == null) return false;
    if (this == o) return true;
    if (getClass() != o.getClass()) return false;

    ShadowGeoPoint that = (ShadowGeoPoint) o;

    if (lat != that.lat) return false;
    if (lng != that.lng) return false;

    return true;
  }

  @Override @Implementation
  public int hashCode() {
    int result = lat;
    result = 31 * result + lng;
    return result;
  }

  @Override @Implementation
  public String toString() {
    return "ShadowGeoPoint{" +
        "lat=" + fromE6(lat) +
        ", lng=" + fromE6(lng) +
        '}';
  }

  /** @deprecated Use {@link #getLatitudeE6()} */
  @Deprecated
  public int getLat() {
    return lat;
  }

  /** @deprecated Use {@link #getLongitudeE6()}. */
  @Deprecated
  public int getLng() {
    return lng;
  }
}

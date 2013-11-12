package org.robolectric.shadows;

import android.location.Location;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.HiddenApi;

import static org.robolectric.Robolectric.shadowOf_;

/**
 * Shadow of {@code Location} that treats it primarily as a data-holder
 * todo: support Location's static utility methods
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Location.class)
public class ShadowLocation {
  private long time;
  private String provider;
  private double latitude;
  private double longitude;
  private float accuracy;
  private float bearing;
  private double altitude;
  private float speed;
  private boolean hasAccuracy;
  private boolean hasAltitude;
  private boolean hasBearing;
  private boolean hasSpeed;

  // Cache the inputs and outputs of computeDistanceAndBearing
  // so calls to distanceTo() and bearingTo() can share work
  private double mLat1 = 0.0;
  private double mLon1 = 0.0;
  private double mLat2 = 0.0;
  private double mLon2 = 0.0;
  private float mDistance = 0.0f;
  private float mInitialBearing = 0.0f;
  // Scratchpad
  private final float[] mResults = new float[2];

  private Bundle extras = new Bundle();

  public void __constructor__(Location l) {
    set(l);
  }

  public void __constructor__(String provider) {
    this.provider = provider;
    time = System.currentTimeMillis();
  }

  @Implementation
  public void set(Location l) {
    time = l.getTime();
    provider = l.getProvider();
    latitude = l.getLatitude();
    longitude = l.getLongitude();
    accuracy = l.getAccuracy();
    bearing = l.getBearing();
    altitude = l.getAltitude();
    speed = l.getSpeed();

    hasAccuracy = l.hasAccuracy();
    hasAltitude = l.hasAltitude();
    hasBearing = l.hasBearing();
    hasSpeed = l.hasSpeed();
  }

  @Implementation
  public String getProvider() {
    return provider;
  }

  @Implementation
  public void setProvider(String provider) {
    this.provider = provider;
  }

  @Implementation
  public long getTime() {
    return time;
  }

  @Implementation
  public void setTime(long time) {
    this.time = time;
  }

  @Implementation
  public float getAccuracy() {
    return accuracy;
  }

  @Implementation
  public void setAccuracy(float accuracy) {
    this.accuracy = accuracy;
    this.hasAccuracy = true;
  }

  @Implementation
  public void removeAccuracy() {
    this.accuracy = 0.0f;
    this.hasAccuracy = false;
  }

  @Implementation
  public boolean hasAccuracy() {
    return hasAccuracy;
  }

  @Implementation
  public double getAltitude() {
    return altitude;
  }

  @Implementation
  public void setAltitude(double altitude) {
    this.altitude = altitude;
    this.hasAltitude = true;
  }

  @Implementation
  public void removeAltitude() {
    this.altitude = 0.0d;
    this.hasAltitude = false;
  }

  @Implementation
  public boolean hasAltitude() {
    return hasAltitude;
  }

  @Implementation
  public float getBearing() {
    return bearing;
  }

  @Implementation
  public void setBearing(float bearing) {
    this.bearing = bearing;
    this.hasBearing = true;
  }

  @Implementation
  public void removeBearing() {
    this.bearing = 0.0f;
    this.hasBearing = false;
  }

  @Implementation
  public boolean hasBearing() {
    return hasBearing;
  }


  @Implementation
  public double getLatitude() {
    return latitude;
  }

  @Implementation
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  @Implementation
  public double getLongitude() {
    return longitude;
  }

  @Implementation
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Implementation
  public float getSpeed() {
    return speed;
  }

  @Implementation
  public void setSpeed(float speed) {
    this.speed = speed;
    this.hasSpeed = true;
  }

  @Implementation
  public void removeSpeed() {
    this.hasSpeed = false;
    this.speed = 0.0f;
  }

  @Implementation
  public boolean hasSpeed() {
    return hasSpeed;
  }

  @Override @Implementation
  public boolean equals(Object o) {
    if (o == null) return false;
    o = shadowOf_(o);
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    if (this == o) return true;

    ShadowLocation that = (ShadowLocation) o;

    if (Double.compare(that.latitude, latitude) != 0) return false;
    if (Double.compare(that.longitude, longitude) != 0) return false;
    if (time != that.time) return false;
    if (provider != null ? !provider.equals(that.provider) : that.provider != null) return false;
    if (accuracy != that.accuracy) return false;
    return true;
  }

  @Override @Implementation
  public int hashCode() {
    int result;
    long temp;
    result = (int) (time ^ (time >>> 32));
    result = 31 * result + (provider != null ? provider.hashCode() : 0);
    temp = latitude != +0.0d ? Double.doubleToLongBits(latitude) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = accuracy != 0f ? Float.floatToIntBits(accuracy) : 0;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override @Implementation
  public String toString() {
    return "Location{" +
        "time=" + time +
        ", provider='" + provider + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", accuracy=" + accuracy +
        '}';
  }

  @HiddenApi @Implementation
  public static void computeDistanceAndBearing(double lat1, double lon1,
      double lat2, double lon2, float[] results) {
    // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
    // using the "Inverse Formula" (section 4)

    int MAXITERS = 20;
    // Convert lat/long to radians
    lat1 *= Math.PI / 180.0;
    lat2 *= Math.PI / 180.0;
    lon1 *= Math.PI / 180.0;
    lon2 *= Math.PI / 180.0;

    double a = 6378137.0; // WGS84 major axis
    double b = 6356752.3142; // WGS84 semi-major axis
    double f = (a - b) / a;
    double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

    double L = lon2 - lon1;
    double A = 0.0;
    double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
    double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

    double cosU1 = Math.cos(U1);
    double cosU2 = Math.cos(U2);
    double sinU1 = Math.sin(U1);
    double sinU2 = Math.sin(U2);
    double cosU1cosU2 = cosU1 * cosU2;
    double sinU1sinU2 = sinU1 * sinU2;

    double sigma = 0.0;
    double deltaSigma = 0.0;
    double cosSqAlpha = 0.0;
    double cos2SM = 0.0;
    double cosSigma = 0.0;
    double sinSigma = 0.0;
    double cosLambda = 0.0;
    double sinLambda = 0.0;

    double lambda = L; // initial guess
    for (int iter = 0; iter < MAXITERS; iter++) {
      double lambdaOrig = lambda;
      cosLambda = Math.cos(lambda);
      sinLambda = Math.sin(lambda);
      double t1 = cosU2 * sinLambda;
      double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
      double sinSqSigma = t1 * t1 + t2 * t2; // (14)
      sinSigma = Math.sqrt(sinSqSigma);
      cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
      sigma = Math.atan2(sinSigma, cosSigma); // (16)
      double sinAlpha = (sinSigma == 0) ? 0.0 :
        cosU1cosU2 * sinLambda / sinSigma; // (17)
      cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
      cos2SM = (cosSqAlpha == 0) ? 0.0 :
        cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

      double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
      A = 1 + (uSquared / 16384.0) * // (3)
        (4096.0 + uSquared *
         (-768 + uSquared * (320.0 - 175.0 * uSquared)));
      double B = (uSquared / 1024.0) * // (4)
        (256.0 + uSquared *
         (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
      double C = (f / 16.0) *
        cosSqAlpha *
        (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
      double cos2SMSq = cos2SM * cos2SM;
      deltaSigma = B * sinSigma * // (6)
        (cos2SM + (B / 4.0) *
         (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
          (B / 6.0) * cos2SM *
          (-3.0 + 4.0 * sinSigma * sinSigma) *
          (-3.0 + 4.0 * cos2SMSq)));

      lambda = L +
        (1.0 - C) * f * sinAlpha *
        (sigma + C * sinSigma *
         (cos2SM + C * cosSigma *
          (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

      double delta = (lambda - lambdaOrig) / lambda;
      if (Math.abs(delta) < 1.0e-12) {
        break;
      }
    }

    float distance = (float) (b * A * (sigma - deltaSigma));
    results[0] = distance;
    if (results.length > 1) {
      float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
        cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
      initialBearing *= 180.0 / Math.PI;
      results[1] = initialBearing;
      if (results.length > 2) {
        float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
          -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
        finalBearing *= 180.0 / Math.PI;
        results[2] = finalBearing;
      }
    }
  }

  private static float[] distanceBetween;

  public static void setDistanceBetween(float[] distanceBetween) {
    ShadowLocation.distanceBetween = distanceBetween;
  }

  /**
   * If it is non-null, returns the mock distance last set with
   * {@link #setDistanceBetween}.
   * Otherwise computes the approximate distance in meters between two
   * locations, and optionally the initial and final bearings of the
   * shortest path between them.  Distance and bearing are defined using the
   * WGS84 ellipsoid.
   *
   * <p> The computed distance is stored in results[0].  If results has length
   * 2 or greater, the initial bearing is stored in results[1]. If results has
   * length 3 or greater, the final bearing is stored in results[2].
   *
   * @param startLatitude the starting latitude
   * @param startLongitude the starting longitude
   * @param endLatitude the ending latitude
   * @param endLongitude the ending longitude
   * @param results an array of floats to hold the results
   *
   * @throws IllegalArgumentException if results is null or has length < 1
   */
  @Implementation
  public static void distanceBetween(double startLatitude, double startLongitude,
    double endLatitude, double endLongitude, float[] results) {
    if (distanceBetween != null && results.length == distanceBetween.length){
      System.arraycopy(distanceBetween, 0, results, 0, results.length);
      return;
    }

    if (results == null || results.length < 1) {
      throw new IllegalArgumentException("results is null or has length < 1");
    }
    computeDistanceAndBearing(startLatitude, startLongitude,
      endLatitude, endLongitude, results);
  }

  /**
   * Returns the approximate distance in meters between this
   * location and the given location.  Distance is defined using
   * the WGS84 ellipsoid.
   *
   * @param dest the destination location
   * @return the approximate distance in meters
   */
  @Implementation
  public float distanceTo(Location dest) {
    // See if we already have the result
    synchronized (mResults) {
      if (latitude != mLat1 || longitude != mLon1 ||
        dest.getLatitude() != mLat2 || dest.getLongitude() != mLon2) {
        computeDistanceAndBearing(latitude, longitude,
          dest.getLatitude(), dest.getLongitude(), mResults);
        mLat1 = latitude;
        mLon1 = longitude;
        mLat2 = dest.getLatitude();
        mLon2 = dest.getLongitude();
        mDistance = mResults[0];
        mInitialBearing = mResults[1];
      }
      return mDistance;
    }
  }

  /**
   * Returns the approximate initial bearing in degrees East of true
   * North when traveling along the shortest path between this
   * location and the given location.  The shortest path is defined
   * using the WGS84 ellipsoid.  Locations that are (nearly)
   * antipodal may produce meaningless results.
   *
   * @param dest the destination location
   * @return the initial bearing in degrees
   */
  @Implementation
  public float bearingTo(Location dest) {
    synchronized (mResults) {
      // See if we already have the result
      if (latitude != mLat1 || longitude != mLon1 ||
              dest.getLatitude() != mLat2 || dest.getLongitude() != mLon2) {
        computeDistanceAndBearing(latitude, longitude,
          dest.getLatitude(), dest.getLongitude(), mResults);
        mLat1 = latitude;
        mLon1 = longitude;
        mLat2 = dest.getLatitude();
        mLon2 = dest.getLongitude();
        mDistance = mResults[0];
        mInitialBearing = mResults[1];
      }
      return mInitialBearing;
    }
  }

  @Implementation
  public Bundle getExtras() {
    return extras;
  }

  @Implementation
  public void setExtras(Bundle extras) {
    this.extras = extras;
  }
}

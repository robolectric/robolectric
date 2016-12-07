package org.robolectric.shadows;

import android.location.Location;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link android.location.Location}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Location.class)
public class ShadowLocation {
  private static float[] distanceBetween;

  /**
   * @deprecated Use a distance/bearing calculation instead to find points that are the desired distance
   * apart.
   */
  @Deprecated
  public static void setDistanceBetween(float[] distanceBetween) {
    ShadowLocation.distanceBetween = distanceBetween;
  }

  @Implementation
  public static void distanceBetween(double startLatitude, double startLongitude,
                                     double endLatitude, double endLongitude, float[] results) {
    if (distanceBetween != null && results.length == distanceBetween.length) {
      System.arraycopy(distanceBetween, 0, results, 0, results.length);
      return;
    }
    directlyOn(Location.class, "distanceBetween",
        ClassParameter.from(double.class, startLatitude),
        ClassParameter.from(double.class, startLongitude),
        ClassParameter.from(double.class, endLatitude),
        ClassParameter.from(double.class, endLongitude),
        ClassParameter.from(float[].class, results));
  }
}

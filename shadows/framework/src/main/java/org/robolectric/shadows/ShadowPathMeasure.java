package org.robolectric.shadows;

import android.graphics.Path;
import android.graphics.PathMeasure;
import java.math.BigDecimal;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(PathMeasure.class)
public class ShadowPathMeasure {

  private CachedPathIteratorFactory mOriginalPathIterator;

  @Implementation
  protected void __constructor__(Path path, boolean forceClosed) {
    if (path != null) {
      ShadowPath shadowPath = (ShadowPath) Shadow.extract(path);
      mOriginalPathIterator =
          new CachedPathIteratorFactory(shadowPath.getJavaShape().getPathIterator(null));
    }
  }

  /**
   * Return the total length of the current contour, or 0 if no path is associated with this measure
   * object.
   */
  @Implementation
  protected float getLength() {
    if (mOriginalPathIterator == null) {
      return 0;
    }

    return mOriginalPathIterator.iterator().getTotalLength();
  }

  /** Note: This is not mathematically correct. */
  @Implementation
  protected boolean getPosTan(float distance, float pos[], float tan[]) {
    if ((pos != null && pos.length < 2) || (tan != null && tan.length < 2)) {
      throw new ArrayIndexOutOfBoundsException();
    }

    // This is not mathematically correct, but the simulation keeps the support library happy.
    if (getLength() > 0) {
      pos[0] = round(distance / getLength(), 4);
      pos[1] = round(distance / getLength(), 4);
    }

    return true;
  }

  private static float round(float d, int decimalPlace) {
    BigDecimal bd = new BigDecimal(d);
    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
    return bd.floatValue();
  }
}

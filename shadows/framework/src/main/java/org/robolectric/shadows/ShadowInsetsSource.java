package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.Rect;
import android.view.InsetsSource;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link InsetsSource}. */
@Implements(value = InsetsSource.class, minSdk = Q, isInAndroidSdk = false)
public class ShadowInsetsSource {
  @RealObject private InsetsSource realInsetsSource;
  @ReflectorObject private InsetsSourceReflector insetsSourceReflector;

  /**
   * Backwards compatible version of {@link InsetsSource#setVisible(boolean)} which changed in U
   * from returning {@code void} to {@link InsetsSource}.
   */
  @CanIgnoreReturnValue
  ShadowInsetsSource setVisible(boolean isVisible) {
    if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
      realInsetsSource.setVisible(isVisible);
    } else {
      insetsSourceReflector.setVisible(isVisible);
    }
    return this;
  }

  /**
   * Backwards compatible version of {@link InsetsSource#setFrame(Rect)} which changed in U from
   * returning {@code void} to {@link InsetsSource}.
   */
  @CanIgnoreReturnValue
  ShadowInsetsSource setFrame(Rect frame) {
    if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
      realInsetsSource.setFrame(frame);
    } else {
      insetsSourceReflector.setFrame(frame);
    }
    return this;
  }

  @ForType(InsetsSource.class)
  interface InsetsSourceReflector {
    // Prior to U this method returned void
    void setFrame(Rect frame);

    // Prior to U this method returned void
    void setVisible(boolean isVisible);
  }
}

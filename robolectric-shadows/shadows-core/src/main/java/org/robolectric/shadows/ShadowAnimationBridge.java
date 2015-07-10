package org.robolectric.shadows;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Bridge between shadows and {@link android.view.animation.Animation}.
 */
@DoNotInstrument
public class ShadowAnimationBridge {
  private Animation realAnimation;

  public ShadowAnimationBridge(Animation realAnimation) {
    this.realAnimation = realAnimation;
  }

  public void applyTransformation(float interpolatedTime, Transformation transformation) {
    ReflectionHelpers.callInstanceMethod(realAnimation, "applyTransformation",
        ClassParameter.from(float.class, interpolatedTime),
        ClassParameter.from(Transformation.class, transformation));
  }
}

package org.robolectric.shadows;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@DoNotInstrument
public class ShadowAnimationBridge {
  private Animation realAnimation;

  public ShadowAnimationBridge(Animation realAnimation) {
    this.realAnimation = realAnimation;
  }

  public void applyTransformation(float interpolatedTime, Transformation transformation) {
    ReflectionHelpers.callInstanceMethodReflectively(realAnimation, "applyTransformation",
        ClassParameter.from(float.class, interpolatedTime),
        ClassParameter.from(Transformation.class, transformation));
  }
}

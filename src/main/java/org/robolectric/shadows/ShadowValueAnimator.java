package org.robolectric.shadows;

import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(ValueAnimator.class)
public class ShadowValueAnimator extends ShadowAnimator {
  private TimeInterpolator interpolator;
  @RealObject
  private ValueAnimator realObject;
  private TypeEvaluator typeEvaluator;

  @Implementation
  public void setInterpolator(TimeInterpolator value) {
    if (value != null) {
      interpolator = value;
    } else {
      interpolator = new LinearInterpolator();
    }
  }

  @Implementation
  public void setEvaluator(TypeEvaluator typeEvaluator) {
    this.typeEvaluator = typeEvaluator;
  }

  @Implementation
  public TimeInterpolator getInterpolator() {
    return interpolator;
  }

  @Implementation
  public static ValueAnimator ofInt (int... values){
    return new ValueAnimator();
  }

  @Implementation
  public boolean isRunning() {
    return false;
  }

  @Implementation @Override
  public long getDuration() {
    return super.getDuration();
  }

  @Implementation
  public void cancel() {

  }

  @Implementation
  public void start() {
    realObject.end();
  }
}

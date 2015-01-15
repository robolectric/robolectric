package org.robolectric.shadows;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.os.Handler;
import android.os.Looper;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ObjectAnimator.class)
public class ShadowObjectAnimator extends ShadowValueAnimator {
  private static boolean pausingEndNotifications;
  private static List<ShadowObjectAnimator> pausedEndNotifications = new ArrayList<>();

  @RealObject
  private ObjectAnimator realObject;
  private String propertyName;
  private float[] floatValues;
  private int[] intValues;
  private Object[] objectValues;
  private Class<?> animationType;
  private boolean isRunning;
  private boolean cancelWasCalled;
  private TypeEvaluator typeEvaluator;

  private void setAnimationType(Class<?> type) {
    animationType = type;
  }

  @Implementation
  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  @Implementation
  public String getPropertyName() {
    return propertyName;
  }

  @Implementation
  public void setFloatValues(float... values) {
    this.floatValues = values;
    Shadows.shadowOf(realObject).setAnimationType(float.class);
    Shadow.directlyOn(realObject, ObjectAnimator.class, "setFloatValues", ClassParameter.from(float[].class, values));
  }

  @Implementation
  public void setIntValues(int... values) {
    this.intValues = values;
    Shadows.shadowOf(realObject).setAnimationType(int.class);
    Shadow.directlyOn(realObject, ObjectAnimator.class, "setIntValues", ClassParameter.from(int[].class, values));
  }

  @Implementation
  public void setObjectValues(Object... values) {
    this.objectValues = values;
    Shadows.shadowOf(realObject).setAnimationType(values[0].getClass());
    Shadow.directlyOn(realObject, ObjectAnimator.class, "setObjectValues", ClassParameter.from(Object[].class, values));
  }

  @Implementation
  public void setEvaluator(TypeEvaluator typeEvaluator) {
    this.typeEvaluator = typeEvaluator;
    super.setEvaluator(typeEvaluator);
  }

  @Implementation
  public ObjectAnimator setDuration(long duration) {
    this.duration = duration;
    return realObject;
  }

  @Implementation
  public void start() {
    isRunning = true;
    String methodName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    final Method setter;
    notifyStart();
    try {
      setter = realObject.getTarget().getClass().getMethod(methodName, animationType);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    int keyFrameCount = 0;
    if (animationType == float.class) {
      keyFrameCount = floatValues.length;
    } else if (animationType == int.class) {
      keyFrameCount = intValues.length;
    } else {
      keyFrameCount = objectValues.length;
    }

    Runnable animationRunnable = new AnimationRunnable(setter);
    if (keyFrameCount > 1) {
      long stepDuration = duration / (keyFrameCount - 1);
      for (int i = 0; i * stepDuration <= duration; ++i) {
        new Handler(Looper.getMainLooper()).postDelayed(animationRunnable, stepDuration * i);
      }
    } else {
      new Handler(Looper.getMainLooper()).postDelayed(animationRunnable, duration);
    }

    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
      @Override
      public void run() {
        isRunning = false;
        if (pausingEndNotifications) {
          pausedEndNotifications.add(ShadowObjectAnimator.this);
        } else {
          notifyEnd();
        }
      }
    }, duration);
  }

  @Override
  @Implementation
  public boolean isRunning() {
    return isRunning;
  }

  @Implementation
  public void cancel() {
    cancelWasCalled = true;
    isRunning = false;
  }

  public boolean cancelWasCalled() {
    return cancelWasCalled;
  }

  public void resetCancelWasCalled() {
    cancelWasCalled = false;
  }

  public static void pauseEndNotifications() {
    pausingEndNotifications = true;
  }

  public static void unpauseEndNotifications() {
    while (pausedEndNotifications.size() > 0) {
      pausedEndNotifications.remove(0).notifyEnd();
    }
    pausingEndNotifications = false;
  }

  private class AnimationRunnable implements Runnable {
    private final Method setter;
    public int index;

    public AnimationRunnable(Method setter) {
      this.setter = setter;
    }

    @Override
    public void run() {
      try {
        if (animationType == float.class) {
          setter.invoke(realObject.getTarget(), floatValues[index]);
        } else if (animationType == int.class) {
          setter.invoke(realObject.getTarget(), intValues[index]);
        } else {
          Object startValue = objectValues[index];
          Object endValue;
          if (index == objectValues.length - 1) {
            endValue = startValue;
          } else {
            endValue = objectValues[index + 1];
          }
          setter.invoke(realObject.getTarget(), typeEvaluator.evaluate((float) index / objectValues.length, startValue, endValue));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        ++index;
      }
    }
  }
}

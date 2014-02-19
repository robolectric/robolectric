package org.robolectric.shadows;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;

import org.robolectric.RobolectricShadowOfLevel16;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings({"UnusedDeclaration"})
@Implements(ObjectAnimator.class)
public class ShadowObjectAnimator extends ShadowValueAnimator {
  private static boolean pausingEndNotifications;
  private static List<ShadowObjectAnimator> pausedEndNotifications = new ArrayList<ShadowObjectAnimator>();

  @RealObject
  private ObjectAnimator realObject;
  private Object target;
  private String propertyName;
  private float[] floatValues;
  private int[] intValues;
  private Class<?> animationType;
  private static final Map<Object, Map<String, ObjectAnimator>> mapsForAnimationTargets = new HashMap<Object, Map<String, ObjectAnimator>>();
  private boolean isRunning;
  private boolean cancelWasCalled;

  @Implementation
  public static ObjectAnimator ofFloat(Object target, String propertyName, float... values) {
    ObjectAnimator result = new ObjectAnimator();

    result.setTarget(target);
    result.setPropertyName(propertyName);
    result.setFloatValues(values);

    getAnimatorMapFor(target).put(propertyName, result);
    return result;
  }

  @Implementation
  public static ObjectAnimator ofInt(Object target, String propertyName, int... values) {
    ObjectAnimator result = new ObjectAnimator();

    result.setTarget(target);
    result.setPropertyName(propertyName);
    result.setIntValues(values);

    getAnimatorMapFor(target).put(propertyName, result);
    return result;
  }

  private static Map<String, ObjectAnimator> getAnimatorMapFor(Object target) {
    Map<String, ObjectAnimator> result = mapsForAnimationTargets.get(target);
    if (result == null) {
      result = new HashMap<String, ObjectAnimator>();
      mapsForAnimationTargets.put(target, result);
    }
    return result;
  }

  private void setAnimationType(Class<?> type) {
    animationType = type;
  }

  @Implementation
  public void setTarget(Object target) {
    this.target = target;
  }

  @Implementation
  public Object getTarget() {
    return target;
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
    RobolectricShadowOfLevel16.shadowOf(realObject).setAnimationType(float.class);
  }

  @Implementation
  public void setIntValues(int... values) {
    this.intValues = values;
    RobolectricShadowOfLevel16.shadowOf(realObject).setAnimationType(int.class);
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
      setter = target.getClass().getMethod(methodName, animationType);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    int keyFrameCount = 0;
    if (animationType == float.class) {
      keyFrameCount = floatValues.length;
    } else if (animationType == int.class) {
      keyFrameCount = intValues.length;
    }

    Runnable animationRunnable = new AnimationRunnable(setter);
    long stepDuration = duration / (keyFrameCount - 1);
    for (int i = 0; i * stepDuration <= duration; ++i) {
      new Handler(Looper.getMainLooper()).postDelayed(animationRunnable, stepDuration * i);
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
  }

  public boolean cancelWasCalled() {
    return cancelWasCalled;
  }

  public void resetCancelWasCalled() {
    cancelWasCalled = false;
  }

  public static Map<String, ObjectAnimator> getAnimatorsFor(Object target) {
    return getAnimatorMapFor(target);
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
          setter.invoke(target, floatValues[index]);
        } else if (animationType == int.class) {
          setter.invoke(target, intValues[index]);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        ++index;
      }
    }
  }
}

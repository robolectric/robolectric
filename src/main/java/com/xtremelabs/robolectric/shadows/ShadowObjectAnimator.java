package com.xtremelabs.robolectric.shadows;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import com.xtremelabs.robolectric.RobolectricShadowOfLevel16;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings({"UnusedDeclaration"})
@Implements(ObjectAnimator.class)
public class ShadowObjectAnimator extends ShadowValueAnimator {
    @RealObject
    private ObjectAnimator realObject;
    private Object target;
    private String propertyName;
    private float[] floatValues;
    private Class<?> animationType;
    private static Map<Object, Map<String, ObjectAnimator>> mapsForAnimationTargets = new HashMap<Object, Map<String, ObjectAnimator>>();

    @Implementation
    public static ObjectAnimator ofFloat(Object target, String propertyName, float... values) {
        ObjectAnimator result = new ObjectAnimator();

        result.setTarget(target);
        result.setPropertyName(propertyName);
        result.setFloatValues(values);
        RobolectricShadowOfLevel16.shadowOf(result).setAnimationType(float.class);

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
    }

    @Implementation
    public ObjectAnimator setDuration(long duration) {
        this.duration = duration;
        return realObject;
    }

    @Implementation
    public void start() {
        String methodName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        final Method setter;
        notifyStart();
        try {
            setter = target.getClass().getMethod(methodName, animationType);
            if (animationType == float.class) {
                setter.invoke(target, floatValues[0]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyEnd();
                    if (animationType == float.class) {
                        setter.invoke(target, floatValues[floatValues.length - 1]);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, duration);
    }

    public static Map<String, ObjectAnimator> getAnimatorsFor(Object target) {
        return getAnimatorMapFor(target);
    }
}

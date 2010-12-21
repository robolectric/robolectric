package com.xtremelabs.robolectric.shadows;

import android.view.MotionEvent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Constructor;

/**
 * Shadow for {@code MotionEvent} that uses reflection to create {@code MotionEvent} objects, which cannot otherwise
 * be constructed.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MotionEvent.class)
public class ShadowMotionEvent {
    @RealObject private MotionEvent realObject;

    private int action;
    private float[] x = new float[2];
    private float[] y = new float[2];

    @Implementation
    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        try {
            Constructor<MotionEvent> constructor = MotionEvent.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            MotionEvent motionEvent = constructor.newInstance();
            ShadowMotionEvent shadowMotionEvent = (ShadowMotionEvent) Robolectric.shadowOf_(motionEvent);
            shadowMotionEvent.x[0] = x;
            shadowMotionEvent.y[0] = y;
            shadowMotionEvent.action = action;
            return motionEvent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Implementation
    public int getAction() {
        return action;
    }

    @Implementation
    public final float getX() {
        return getX(0);
    }

    @Implementation
    public final float getY() {
        return getY(0);
    }

    @Implementation
    public final float getX(int pointerIndex) {
        return x[pointerIndex];
    }

    @Implementation
    public final float getY(int pointerIndex) {
        return y[pointerIndex];
    }

    public MotionEvent setPointer2(float x, float y) {
        this.x[1] = x;
        this.y[1] = y;
        return realObject;
    }
}

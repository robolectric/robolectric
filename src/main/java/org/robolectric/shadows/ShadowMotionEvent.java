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
    private int pointerCount = 1;
    private long downTime;
    private long eventTime;
    private int[] pointerIds = new int[2];
    private int pointerIndex;

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
            shadowMotionEvent.downTime = downTime;
            shadowMotionEvent.eventTime = eventTime;
            return motionEvent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Implementation
    public static MotionEvent obtain(MotionEvent motionEvent) {
        return obtain(motionEvent.getDownTime(), motionEvent.getEventTime(), motionEvent.getAction(), motionEvent.getX(), motionEvent.getY(), motionEvent.getMetaState());
    }

    @Implementation
    public int getAction() {
        return action | (pointerIndex << MotionEvent.ACTION_POINTER_ID_SHIFT);
    }

    @Implementation
    public void setAction(int action ) {
        this.action = action;
    }
    
    @Implementation
    public float getRawX() {
        return getX();
    }

    @Implementation
    public float getRawY() {
        return getY();
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
    
    @Implementation
    public final int getPointerCount() {
    	return pointerCount;
    }

    @Implementation
    public final long getEventTime() {
        return eventTime;
    }

    @Implementation
    public final long getDownTime() {
        return downTime;
    }

    @Implementation
    public final int getPointerId(int index) {
        return pointerIds[index];
    }

    @Implementation
    public final int findPointerIndex(int id) {
        for (int i = 0; i < pointerIds.length; i++) {
            int pointerId = pointerIds[i];

            if (pointerId == id) {
                return i;
            }
        }
        return -1;
    }

    @Implementation
    public final int getActionMasked() {
        return action;
    }

    @Implementation
    public final int getActionIndex() {
        return pointerIndex;
    }

    @Implementation
    public final float getPressure(int pointerIndex) {
        return 1.0f;
    }

    @Implementation
    public final void setLocation(float x, float y) {
        this.x[0] = x;
        this.y[0] = y;
    }

    public MotionEvent setPointer2(float x, float y) {
        this.x[1] = x;
        this.y[1] = y;
        pointerCount = 2;
        return realObject;
    }

    public void setPointerIndex(int pointerIndex) {
        this.pointerIndex = pointerIndex;
    }

    public void setPointerIds(int index0PointerId, int index1PointerId) {
        pointerIds[0] = index0PointerId;
        pointerIds[1] = index1PointerId;
    }
}

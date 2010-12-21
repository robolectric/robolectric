package com.xtremelabs.robolectric.shadows;

import android.view.MotionEvent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.lang.reflect.Constructor;

/**
 * Shadow for {@code MotionEvent} that uses reflection to create {@code MotionEvent} objects, which cannot otherwise
 * be constructed.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MotionEvent.class)
public class ShadowMotionEvent {
    private int action;
    private float x;
    private float y;

    @Implementation
    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        try {
            Constructor<MotionEvent> constructor = MotionEvent.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            MotionEvent motionEvent = constructor.newInstance();
            ShadowMotionEvent shadowMotionEvent = (ShadowMotionEvent) Robolectric.shadowOf_(motionEvent);
            shadowMotionEvent.x = x;
            shadowMotionEvent.y = y;
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
        return x;
    }

    @Implementation
    public final float getY() {
        return y;
    }
}

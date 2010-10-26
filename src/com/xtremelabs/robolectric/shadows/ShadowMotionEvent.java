package com.xtremelabs.robolectric.shadows;

import android.view.MotionEvent;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.lang.reflect.Constructor;

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
            ShadowMotionEvent shadowMotionEvent = (ShadowMotionEvent) ProxyDelegatingHandler.getInstance().shadowFor(motionEvent);
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

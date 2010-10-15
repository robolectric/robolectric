package com.xtremelabs.robolectric.fakes;

import android.view.MotionEvent;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.lang.reflect.Constructor;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MotionEvent.class)
public class FakeMotionEvent {
    private int action;
    private float x;
    private float y;

    @Implementation
    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        try {
            Constructor<MotionEvent> constructor = MotionEvent.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            MotionEvent motionEvent = constructor.newInstance();
            FakeMotionEvent fakeEvent = (FakeMotionEvent) ProxyDelegatingHandler.getInstance().proxyFor(motionEvent);
            fakeEvent.x = x;
            fakeEvent.y = y;
            fakeEvent.action = action;
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

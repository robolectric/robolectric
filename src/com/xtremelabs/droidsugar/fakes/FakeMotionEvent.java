package com.xtremelabs.droidsugar.fakes;

import android.view.MotionEvent;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implements;

import java.lang.reflect.Constructor;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MotionEvent.class)
public class FakeMotionEvent {
    private int action;

    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        try {
            Constructor<MotionEvent> constructor = MotionEvent.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            MotionEvent motionEvent = constructor.newInstance();
            ((FakeMotionEvent) ProxyDelegatingHandler.getInstance().proxyFor(motionEvent)).action = action;
            return motionEvent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getAction() {
        return action;
    }
}

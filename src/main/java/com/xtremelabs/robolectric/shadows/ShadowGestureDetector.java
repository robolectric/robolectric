package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GestureDetector.class)
public class ShadowGestureDetector {
    @RealObject
    private GestureDetector realObject;

    private MotionEvent onTouchEventMotionEvent;
    private boolean onTouchEventNextReturnValue = true;

    private GestureDetector.OnGestureListener listener;
    private static GestureDetector lastActiveGestureDetector;

    public void __constructor__(GestureDetector.OnGestureListener listener) {
        __constructor__(null, listener);
    }

    public void __constructor__(Context context, GestureDetector.OnGestureListener listener) {
        this.listener = listener;
    }

    @Implementation
    public boolean onTouchEvent(MotionEvent ev) {
        lastActiveGestureDetector = realObject;
        onTouchEventMotionEvent = ev;
        return onTouchEventNextReturnValue;
    }

    public MotionEvent getOnTouchEventMotionEvent() {
        return onTouchEventMotionEvent;
    }

    public void reset() {
        onTouchEventMotionEvent = null;
    }

    public void setNextOnTouchEventReturnValue(boolean nextReturnValue) {
        onTouchEventNextReturnValue = nextReturnValue;
    }

    public GestureDetector.OnGestureListener getListener() {
        return listener;
    }

    public static GestureDetector getLastActiveDetector() {
        return lastActiveGestureDetector;
    }
}

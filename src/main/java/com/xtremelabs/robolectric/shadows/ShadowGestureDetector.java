package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GestureDetector.class)
public class ShadowGestureDetector {
    private MotionEvent onTouchEventMotionEvent;
    private boolean onTouchEventNextReturnValue = true;
    private GestureDetector.OnGestureListener listener;

    public void __constructor__(GestureDetector.OnGestureListener listener) {
        __constructor__(null, listener);
    }

    public void __constructor__(Context context, GestureDetector.OnGestureListener listener) {
        this.listener = listener;
    }

    @Implementation
    public boolean onTouchEvent(MotionEvent ev) {
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
}

package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ScaleGestureDetector.class)
public class ShadowScaleGestureDetector {

    private MotionEvent onTouchEventMotionEvent;
    private ScaleGestureDetector.OnScaleGestureListener listener;
    private float scaleFactor = 1;
    private float focusX;
    private float focusY;

    @Implementation
    public void __constructor__(Context context, ScaleGestureDetector.OnScaleGestureListener listener) {
        this.listener = listener;
    }

    @Implementation
    public boolean onTouchEvent(MotionEvent event) {
        onTouchEventMotionEvent = event;
        return true;
    }

    public MotionEvent getOnTouchEventMotionEvent() {
        return onTouchEventMotionEvent;
    }

    public void reset() {
        onTouchEventMotionEvent = null;
        scaleFactor = 1;
        focusX = 0;
        focusY = 0;
    }

    public ScaleGestureDetector.OnScaleGestureListener getListener() {
        return listener;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Implementation
    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setFocusXY(float focusX, float focusY) {
        this.focusX = focusX;
        this.focusY = focusY;
    }

    @Implementation
    public float getFocusX(){
        return focusX;
    }

    @Implementation
    public float getFocusY(){
        return focusY;
    }
}

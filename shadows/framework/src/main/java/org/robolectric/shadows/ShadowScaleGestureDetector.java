package org.robolectric.shadows;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ScaleGestureDetector.class)
public class ShadowScaleGestureDetector {

  @ReflectorObject ScaleGestureDetectorReflector scaleGestureDetectorReflector;
  private MotionEvent onTouchEventMotionEvent;
  private ScaleGestureDetector.OnScaleGestureListener listener;
  private Float scaleFactor;
  private Float focusX;
  private Float focusY;

  @Implementation
  protected void __constructor__(
      Context context, ScaleGestureDetector.OnScaleGestureListener listener) {
    scaleGestureDetectorReflector.__constructor__(context, listener);
    this.listener = listener;
  }

  @Implementation
  protected boolean onTouchEvent(MotionEvent event) {
    onTouchEventMotionEvent = event;
    return scaleGestureDetectorReflector.onTouchEvent(event);
  }

  public MotionEvent getOnTouchEventMotionEvent() {
    return onTouchEventMotionEvent;
  }

  public void reset() {
    onTouchEventMotionEvent = null;
    scaleFactor = null;
    focusX = null;
    focusY = null;
  }

  public ScaleGestureDetector.OnScaleGestureListener getListener() {
    return listener;
  }

  public void setScaleFactor(float scaleFactor) {
    this.scaleFactor = scaleFactor;
  }

  @Implementation
  protected float getScaleFactor() {
    return scaleFactor != null ? scaleFactor : scaleGestureDetectorReflector.getScaleFactor();
  }

  public void setFocusXY(float focusX, float focusY) {
    this.focusX = focusX;
    this.focusY = focusY;
  }

  @Implementation
  protected float getFocusX() {
    return focusX != null ? focusX : scaleGestureDetectorReflector.getFocusX();
  }

  @Implementation
  protected float getFocusY() {
    return focusY != null ? focusY : scaleGestureDetectorReflector.getFocusY();
  }

  @ForType(ScaleGestureDetector.class)
  private interface ScaleGestureDetectorReflector {
    @Direct
    void __constructor__(Context context, ScaleGestureDetector.OnScaleGestureListener listener);

    @Direct
    boolean onTouchEvent(MotionEvent event);

    @Direct
    float getScaleFactor();

    @Direct
    float getFocusX();

    @Direct
    float getFocusY();
  }
}

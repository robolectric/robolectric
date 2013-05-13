package org.robolectric.shadows;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static android.view.GestureDetector.OnDoubleTapListener;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GestureDetector.class)
public class ShadowGestureDetector {
  @RealObject
  private GestureDetector realObject;

  private MotionEvent onTouchEventMotionEvent;
  private boolean onTouchEventNextReturnValue = true;

  private GestureDetector.OnGestureListener listener;
  private static GestureDetector lastActiveGestureDetector;
  private OnDoubleTapListener onDoubleTapListener;

  public void __constructor__(GestureDetector.OnGestureListener listener) {
    __constructor__(null, listener);
  }

  public void __constructor__(Context context, GestureDetector.OnGestureListener listener) {
    this.listener = listener;
    if (listener instanceof OnDoubleTapListener) {
      setOnDoubleTapListener((OnDoubleTapListener) listener);
    }
  }

  @Implementation
  public boolean onTouchEvent(MotionEvent ev) {
    lastActiveGestureDetector = realObject;
    onTouchEventMotionEvent = ev;
    return onTouchEventNextReturnValue;
  }

  @Implementation
  public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
    this.onDoubleTapListener = onDoubleTapListener;
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

  public OnDoubleTapListener getOnDoubleTapListener() {
    return onDoubleTapListener;
  }
}

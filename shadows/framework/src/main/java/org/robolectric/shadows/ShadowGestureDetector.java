package org.robolectric.shadows;

import static android.view.GestureDetector.OnDoubleTapListener;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GestureDetector.class)
public class ShadowGestureDetector {
  @RealObject private GestureDetector realObject;

  private static GestureDetector lastActiveGestureDetector;

  private MotionEvent onTouchEventMotionEvent;
  private GestureDetector.OnGestureListener listener;
  private OnDoubleTapListener onDoubleTapListener;

  @Filter(order = Order.AFTER)
  protected void __constructor__(
      Context context, GestureDetector.OnGestureListener listener, Handler handler) {
    this.listener = listener;
  }

  @Implementation
  protected boolean onTouchEvent(MotionEvent ev) {
    lastActiveGestureDetector = realObject;
    onTouchEventMotionEvent = ev;

    return reflector(GestureDetectorReflector.class, realObject).onTouchEvent(ev);
  }

  @Filter(order = Order.AFTER)
  protected void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
    this.onDoubleTapListener = onDoubleTapListener;
  }

  public MotionEvent getOnTouchEventMotionEvent() {
    return onTouchEventMotionEvent;
  }

  public void reset() {
    onTouchEventMotionEvent = null;
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

  @ForType(GestureDetector.class)
  interface GestureDetectorReflector {

    @Direct
    boolean onTouchEvent(MotionEvent ev);
  }
}

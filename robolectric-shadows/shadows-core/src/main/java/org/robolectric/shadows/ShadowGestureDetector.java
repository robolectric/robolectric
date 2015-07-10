package org.robolectric.shadows;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.Shadow;

import static android.view.GestureDetector.OnDoubleTapListener;
import static org.robolectric.internal.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

/**
 * Shadow for {@link android.view.GestureDetector}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(GestureDetector.class)
public class ShadowGestureDetector {
  @RealObject
  private GestureDetector realObject;

  private static GestureDetector lastActiveGestureDetector;

  private MotionEvent onTouchEventMotionEvent;
  private GestureDetector.OnGestureListener listener;
  private OnDoubleTapListener onDoubleTapListener;

  public void __constructor__(Context context, GestureDetector.OnGestureListener listener, Handler handler) {
    Shadow.invokeConstructor(GestureDetector.class, realObject,
        from(Context.class, context),
        from(GestureDetector.OnGestureListener.class, listener),
        from(Handler.class, handler));
    this.listener = listener;
  }

  @Implementation
  public boolean onTouchEvent(MotionEvent ev) {
    lastActiveGestureDetector = realObject;
    onTouchEventMotionEvent = ev;

    return directlyOn(realObject, GestureDetector.class).onTouchEvent(ev);
  }

  @Implementation
  public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
    directlyOn(realObject, GestureDetector.class).setOnDoubleTapListener(onDoubleTapListener);
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
}

package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Fake implementation of GLSurfaceView */
@Implements(GLSurfaceView.class)
public class ShadowGLSurfaceView extends ShadowSurfaceView {
  private static final ConcurrentLinkedQueue<WeakReference<GLSurfaceView>> surfaceViews =
      new ConcurrentLinkedQueue<>();

  @RealObject private GLSurfaceView realSurfaceView;

  @Implementation
  protected void __constructor__(Context context) {
    reflector(GLSurfaceViewReflector.class, realSurfaceView).__constructor__(context);
    surfaceViews.add(new WeakReference<>(realSurfaceView));
  }

  @Implementation
  protected void __constructor__(Context context, AttributeSet attrs) {
    reflector(GLSurfaceViewReflector.class, realSurfaceView).__constructor__(context, attrs);
    surfaceViews.add(new WeakReference<>(realSurfaceView));
  }

  @Implementation
  protected void onPause() {}

  @Implementation
  protected void onResume() {}

  private static void requestExitAndWaitGlThread(GLSurfaceView view) {
    Object glThread = reflector(GLSurfaceViewReflector.class, view).getGLThread();
    if (glThread != null) {
      ReflectionHelpers.callInstanceMethod(glThread, "requestExitAndWait");
    }
  }

  @Resetter
  public static void reset() {
    WeakReference<GLSurfaceView> surfaceView;
    while ((surfaceView = surfaceViews.poll()) != null) {
      GLSurfaceView view = surfaceView.get();
      if (view != null) {
        requestExitAndWaitGlThread(view);
      }
    }
  }

  @ForType(GLSurfaceView.class)
  interface GLSurfaceViewReflector {
    @Accessor("mGLThread")
    Object getGLThread();

    @Direct
    void __constructor__(Context context);

    @Direct
    void __constructor__(Context context, AttributeSet attrs);
  }
}

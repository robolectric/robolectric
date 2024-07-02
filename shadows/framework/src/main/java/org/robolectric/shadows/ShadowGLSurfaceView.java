package org.robolectric.shadows;

import android.opengl.GLSurfaceView;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Fake implementation of GLSurfaceView */
@Implements(GLSurfaceView.class)
public class ShadowGLSurfaceView extends ShadowSurfaceView {

  private static final String TAG = "ShadowGLSurfaceView";

  @Implementation
  protected void onPause() {}

  @Implementation
  protected void onResume() {}

  @Implementation
  protected void onDetachedFromWindow() {
    stopGLThread();
  }

  @Implementation
  protected void finalize() throws Throwable {
    try {
      stopGLThread();
    } finally {
      super.finalize();
    }
  }

  private void stopGLThread() {
    try {
      Field glThreadField = GLSurfaceView.class.getDeclaredField("mGLThread");
      glThreadField.setAccessible(true);
      Thread glThread = (Thread) glThreadField.get(realView);
      if (glThread != null) {
        Method requestExitAndWait = glThread.getClass().getMethod("requestExitAndWait");
        requestExitAndWait.invoke(glThread);
      }
    } catch (Exception e) {
      Log.e(TAG, "Exception occurred while stopping GLThread: " + e.getMessage());
      e.printStackTrace();
    }
  }
}

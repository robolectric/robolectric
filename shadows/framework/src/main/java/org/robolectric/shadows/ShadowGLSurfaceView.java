package org.robolectric.shadows;

import android.opengl.GLSurfaceView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/** Fake implementation of GLSurfaceView */
@Implements(GLSurfaceView.class)
public class ShadowGLSurfaceView extends ShadowSurfaceView {
  @Implementation
  protected void onPause() {}

  @Implementation
  protected void onResume() {}

  @Resetter
  public static void reset() {
    try {
      // Use reflection to access the private mGLThread field
      Field glThreadField = GLSurfaceView.class.getDeclaredField("mGLThread");
      glThreadField.setAccessible(true);
      Object glThread = glThreadField.get(null);

      // If the GL thread exists, request its exit and wait for it to terminate
      if (glThread != null) {

        Method requestExitAndWait = glThread.getClass().getMethod("requestExitAndWait");
        if (requestExitAndWait != null) {
          requestExitAndWait.invoke(glThread);
        } else {
          System.err.println("GLThread does not have a method named requestExitAndWait()");
        }
      }
    } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      System.err.println("Exception occurred while resetting GLSurfaceView: " + e.getMessage());
      e.printStackTrace();
    }
  }
}

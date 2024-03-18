package org.robolectric.shadows;

import android.opengl.GLSurfaceView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.Field;

/** Fake implementation of GLSurfaceView */
@Implements(GLSurfaceView.class)
public class ShadowGLSurfaceView extends ShadowSurfaceView {

  // Placeholder class for GLThread
  private static class GLThread {
    private volatile boolean running = true;

    public void requestExitAndWait() {
      // Set the running flag to false to signal the thread to stop
      running = false;

      // Perform any necessary cleanup or synchronization before exiting
      // For example, you might wait for rendering to finish or release OpenGL resources
      try {
        // Simulate waiting for rendering to finish
        // This could involve joining with other threads or waiting for a condition
        Thread.sleep(1000); // Simulate waiting for 1 second
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      // Print a message indicating that the thread has exited
      System.out.println("GLThread exited");
    }

    public void run() {
      // Simulate rendering OpenGL graphics
      while (running) {
        // Render OpenGL graphics
        try {
          // Simulate rendering delay
          Thread.sleep(16); // Simulate rendering at 60 frames per second
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }


  // Helper method to retrieve the mGLThread field using reflection
  private GLThread getGLThread(GLSurfaceView glSurfaceView) {
    try {
      Field glThreadField = GLSurfaceView.class.getDeclaredField("mGLThread");
      glThreadField.setAccessible(true); // Make the field accessible
      return (GLThread) glThreadField.get(glSurfaceView);
    } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Adjusted method to accept GLSurfaceView instance as parameter
  public void callOnDetachedFromWindow(GLSurfaceView glSurfaceView) {
    // Terminate GLThread if it's still running
    if (glSurfaceView != null) {
      GLThread glThread = getGLThread(glSurfaceView);
      if (glThread != null) {
        glThread.requestExitAndWait();
      }
    }
  }

  @Implementation
  protected void onPause() {}

  @Implementation
  protected void onResume() {}
}

package org.robolectric.shadows;

import android.opengl.GLSurfaceView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Fake implementation of GLSurfaceView */
@Implements(GLSurfaceView.class)
public class ShadowGLSurfaceView extends ShadowSurfaceView {
  private Thread glThread;

  @Implementation
  protected void onPause() {}

  @Implementation
  protected void onResume() {}

  @Override
  public void callOnDetachedFromWindow() {
    // Terminate the GL thread if it's still running
    if (glThread != null && glThread.isAlive()) {
      glThread.interrupt();
      glThread = null;
    }
    super.callOnDetachedFromWindow();
  }
}
package org.robolectric.shadows;

import android.opengl.GLSurfaceView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Fake implementation of GLSurfaceView */
@Implements(GLSurfaceView.class)
public class ShadowGLSurfaceView extends ShadowSurfaceView {
  @Implementation
  protected void onPause() {}

  @Implementation
  protected void onResume() {}
}

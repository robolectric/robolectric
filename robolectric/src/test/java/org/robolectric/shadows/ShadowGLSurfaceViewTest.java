package org.robolectric.shadows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.opengl.GLSurfaceView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/**
 * Unit tests for {@link ShadowGLSurfaceView}.
 */
@RunWith(RobolectricTestRunner.class)
public class ShadowGLSurfaceViewTest {

  @Test
  public void queueEvent() {
    GLSurfaceView surfaceView = new GLSurfaceView(RuntimeEnvironment.application);
    Runnable event = mock(Runnable.class);
    surfaceView.queueEvent(event);
    verify(event).run();
  }
}

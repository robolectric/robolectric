package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Parcel;
import android.view.Surface;
import android.view.SurfaceControl;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.ForType;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeSurfaceTest {
  @Test
  public void surface_construction() {
    // Invoke the public/hidden no-op constructor. Although it's public, it's not available in a
    // Gradle environment, because integration_tests/nativegraphics is a com.android.library
    // project,
    // which uses the stubs jar, so only public signatures available during compile-time.
    Surface s = Shadow.newInstanceOf(Surface.class);
    s.release();
  }

  @Test
  public void surface_construction_surfaceTexture() {
    SurfaceTexture st = new SurfaceTexture(false);
    Surface s = new Surface(st);
    s.release();
  }

  @Config(minSdk = Q)
  @Test
  public void surface_fromSurfaceControl_doesNotThrow() {
    SurfaceControl control =
        new SurfaceControl.Builder().setName("test").setBufferSize(100, 100).build();
    Surface surface = new Surface(control);
    surface.release();
  }

  /** This test simulates what occurs in View#startDragAndDrop. */
  @Config(minSdk = CINNAMON_BUN)
  @Test
  public void copyFrom_surfaceControl_lockHardwareCanvas() {
    SurfaceControl control =
        new SurfaceControl.Builder().setName("test").setBufferSize(100, 100).build();
    Surface surface = Shadow.newInstanceOf(Surface.class);
    reflector(SurfaceReflector.class, surface).copyFrom(control);
    assertThat(surface.isValid()).isTrue();
    Canvas canvas = surface.lockHardwareCanvas();
    canvas.drawColor(Color.RED);
    surface.unlockCanvasAndPost(canvas);
    surface.release();
  }

  @Test
  public void surface_toFromParcel_doesNotThrow() {
    Surface surface = Shadow.newInstanceOf(Surface.class);
    Parcel parcel = Parcel.obtain();

    surface.writeToParcel(parcel, /* flags= */ 0);
    parcel.setDataPosition(0);
    surface.readFromParcel(parcel);
  }

  @ForType(Surface.class)
  interface SurfaceReflector {
    void copyFrom(SurfaceControl other);
  }
}

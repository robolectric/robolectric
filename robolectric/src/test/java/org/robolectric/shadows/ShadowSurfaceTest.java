package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.View.DragShadowBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dalvik.system.CloseGuard;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowSurface}. */
@RunWith(AndroidJUnit4.class)
public class ShadowSurfaceTest {
  private final SurfaceTexture texture = new SurfaceTexture(0);
  private final Surface surface = new Surface(texture);

  @After
  public void tearDown() {
    surface.release();
  }

  @Test
  public void getSurfaceTexture_returnsSurfaceTexture() {
    assertThat(shadowOf(surface).getSurfaceTexture()).isEqualTo(texture);
  }

  @Test
  public void release_doesNotThrow() {
    surface.release();
  }

  @Test
  public void toString_returnsNotEmptyString() {
    assertThat(surface.toString()).isNotEmpty();
  }

  @Test
  public void newSurface_doesNotResultInCloseGuardError() throws Throwable {
    final AtomicBoolean closeGuardWarned = new AtomicBoolean(false);
    CloseGuard.Reporter originalReporter = CloseGuard.getReporter();
    try {
      CloseGuard.setReporter((s, throwable) -> closeGuardWarned.set(true));
      MySurface surface = new MySurface();
      surface.finalize();
      assertThat(closeGuardWarned.get()).isFalse();
      surface.release();
    } finally {
      CloseGuard.setReporter(originalReporter);
    }
  }

  @Test
  public void lockCanvas_returnsCanvas() {
    assertThat(surface.lockCanvas(new Rect())).isInstanceOf(Canvas.class);
  }

  @Test
  @Config(minSdk = M)
  public void lockHardwareCanvas_returnsCanvas() {
    Canvas canvas = surface.lockHardwareCanvas();
    assertThat(canvas.isHardwareAccelerated()).isTrue();
  }

  @Test
  public void lockCanvasTwice_throwsIllegalStateException() {
    surface.lockCanvas(new Rect());

    assertThrows(IllegalStateException.class, () -> surface.lockCanvas(new Rect()));
  }

  @Test
  public void lockCanvasOnReleasedSurface_throwsIllegalStateException() {
    surface.release();

    assertThrows(IllegalStateException.class, () -> surface.lockCanvas(new Rect()));
  }

  @Test
  public void unlockCanvasOnReleasedSurface_throwsIllegalStateException() {
    Canvas canvas = surface.lockCanvas(new Rect());
    surface.release();

    assertThrows(IllegalStateException.class, () -> surface.unlockCanvasAndPost(canvas));
  }

  @Test
  public void unlockCanvasOnUnLockedSurface_throwsIllegalStateException() {
    Canvas canvas = surface.lockCanvas(new Rect());
    surface.unlockCanvasAndPost(canvas);

    assertThrows(IllegalStateException.class, () -> surface.unlockCanvasAndPost(canvas));
  }

  @Test
  public void unlockCanvasAndPost_triggersFrameUpdateInSurfaceTexture() {
    AtomicBoolean listenerCallBackCalled = new AtomicBoolean(false);

    texture.setOnFrameAvailableListener((surfaceTexture) -> listenerCallBackCalled.set(true));
    Canvas canvas = surface.lockCanvas(new Rect());
    surface.unlockCanvasAndPost(canvas);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(listenerCallBackCalled.get()).isTrue();
  }

  @Config(minSdk = M)
  @Test
  public void unlockCanvasAndPost_triggersFrameUpdateInSurfaceTexture_hardwareCanvas() {
    AtomicBoolean listenerCallBackCalled = new AtomicBoolean(false);

    texture.setOnFrameAvailableListener((surfaceTexture) -> listenerCallBackCalled.set(true));
    Canvas canvas = surface.lockHardwareCanvas();
    surface.unlockCanvasAndPost(canvas);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(listenerCallBackCalled.get()).isTrue();
  }

  /**
   * This test simulates what occurs in {@link android.view.View#startDragAndDrop(ClipData,
   * DragShadowBuilder, Object, int)}..
   */
  @Config(minSdk = Q)
  @Test
  public void copyFrom_surfaceControl_lockHardwareCavnvas() {
    SurfaceSession session = new SurfaceSession();
    SurfaceControl surfaceControl =
        new SurfaceControl.Builder(session).setBufferSize(100, 100).setName("").build();
    Surface surface2 = new Surface();
    surface2.copyFrom(surfaceControl);
    Canvas canvas = surface2.lockHardwareCanvas();
    assertThat(canvas).isNotNull();
    surface2.release();
  }

  /** Used to expose the finalize method for testing purposes. */
  static class MySurface extends Surface {
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
  }
}

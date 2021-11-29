package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowSurfaceTexture}. */
@RunWith(AndroidJUnit4.class)
public class ShadowSurfaceTextureTest {
  private final SurfaceTexture surfaceTexture = new SurfaceTexture(0);

  @Test
  @Config(minSdk = LOLLIPOP)
  public void surfaceUnlockAndPost_callsBackListener() throws Exception {
    final AtomicBoolean frameCallback = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);
    HandlerThread cbHandlerThread = new HandlerThread("CallBackHandlerThread");
    cbHandlerThread.start();
    Handler handler = new Handler(cbHandlerThread.getLooper());

    surfaceTexture.setOnFrameAvailableListener(
        (texture) -> {
          frameCallback.set(true);
          latch.countDown();
        },
        handler);
    Surface surface = new Surface(surfaceTexture);
    surface.unlockCanvasAndPost(surface.lockCanvas(new Rect()));
    latch.await();

    assertThat(frameCallback.get()).isTrue();
    surface.release();
  }
}

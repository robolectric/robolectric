package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dalvik.system.CloseGuard;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowSurfaceTest {
  private final SurfaceTexture texture = new SurfaceTexture(0);
  private final Surface surface = new Surface(texture);

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
    } finally {
      CloseGuard.setReporter(originalReporter);
    }
  }

  /** Used to expose the finalize method for testing purposes. */
  static class MySurface extends Surface {
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
  }
}

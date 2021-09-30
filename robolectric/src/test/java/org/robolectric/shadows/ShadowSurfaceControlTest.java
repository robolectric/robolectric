package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.view.SurfaceControl;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dalvik.system.CloseGuard;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Tests for {@link org.robolectric.shadows.ShadowSurfaceControl} */
@RunWith(AndroidJUnit4.class)
public class ShadowSurfaceControlTest {
  // The spurious CloseGuard warnings happens in Q, where the CloseGuard is always opened.
  @Test
  @Config(sdk = Q)
  public void newSurfaceControl_doesNotResultInCloseGuardError() {
    final AtomicBoolean closeGuardWarned = new AtomicBoolean(false);
    CloseGuard.Reporter originalReporter = CloseGuard.getReporter();
    try {
      CloseGuard.setReporter((s, throwable) -> closeGuardWarned.set(true));
      SurfaceControl sc = new SurfaceControl();
      ReflectionHelpers.callInstanceMethod(sc, "finalize");
      assertThat(closeGuardWarned.get()).isFalse();
    } finally {
      CloseGuard.setReporter(originalReporter);
    }
  }
}

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.view.SurfaceControl;
import android.view.SurfaceSession;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dalvik.system.CloseGuard;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Tests for {@link org.robolectric.shadows.ShadowSurfaceControl} */
@Config(minSdk = JELLY_BEAN_MR2)
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

  @Test
  @Config(maxSdk = O)
  public void newSurfaceControl_isNotNull() {
    SurfaceControl surfaceControl =
        ReflectionHelpers.callConstructor(
            SurfaceControl.class,
            ClassParameter.from(SurfaceSession.class, new SurfaceSession()),
            ClassParameter.from(String.class, "surface_control_name"),
            ClassParameter.from(int.class, 100),
            ClassParameter.from(int.class, 100),
            ClassParameter.from(int.class, 0),
            ClassParameter.from(int.class, SurfaceControl.HIDDEN));

    assertThat(surfaceControl).isNotNull();
  }

  @Test
  @Config(sdk = P)
  public void build_isNotNull() {
    SurfaceControl.Builder surfaceControlBuilder =
        new SurfaceControl.Builder(new SurfaceSession()).setName("surface_control_name");
    ReflectionHelpers.callInstanceMethod(
        surfaceControlBuilder,
        "setSize",
        ClassParameter.from(int.class, 100),
        ClassParameter.from(int.class, 100));
    SurfaceControl surfaceControl = surfaceControlBuilder.build();

    assertThat(surfaceControl).isNotNull();
  }

  @Test
  @Config(minSdk = Q)
  public void build_isValid() {
    SurfaceControl surfaceControl =
        new SurfaceControl.Builder(new SurfaceSession()).setName("surface_control_name").build();

    assertThat(surfaceControl.isValid()).isTrue();
  }
}

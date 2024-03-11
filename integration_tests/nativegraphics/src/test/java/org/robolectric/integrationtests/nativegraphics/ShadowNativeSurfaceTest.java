package org.robolectric.integrationtests.nativegraphics;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeSurfaceTest {
  @Test
  public void surface_construction() {
    // Invoke the public/hidden no-op constructor. Although it's public, it's not available in a
    // Gradle environment, because integration_tests/nativegrapics is a com.android.library project,
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
}

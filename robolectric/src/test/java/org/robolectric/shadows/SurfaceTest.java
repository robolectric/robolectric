package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import android.view.Surface;
import android.graphics.SurfaceTexture;
import static org.robolectric.Robolectric.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RobolectricBase.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class SurfaceTest {
  private final SurfaceTexture texture = new SurfaceTexture(0);
  private final Surface surface = new Surface(texture);

  @Test
  public void getSurfaceTexture_returnsSurfaceTexture() throws Exception {
    assertThat(shadowOf(surface).getSurfaceTexture()).isEqualTo(texture);
  }
}

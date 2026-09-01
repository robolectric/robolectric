package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.hardware.camera2.extension.CameraOutputSurface;
import android.os.Build.VERSION_CODES;
import android.util.Size;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
public final class CameraOutputSurfaceBuilderTest {

  @Test
  public void build_createsCameraOutputSurface() {
    Surface surface = new Surface();
    Size size = new Size(1920, 1080);
    CameraOutputSurface outputSurface =
        CameraOutputSurfaceBuilder.newBuilder().setSurface(surface).setSize(size).build();

    assertThat(outputSurface).isNotNull();
    assertThat(outputSurface.getSurface()).isEqualTo(surface);
    assertThat(outputSurface.getSize()).isEqualTo(size);
  }
}

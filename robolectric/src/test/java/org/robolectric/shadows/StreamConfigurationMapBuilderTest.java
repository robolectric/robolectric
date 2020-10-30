package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build.VERSION_CODES;
import android.util.Size;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link StreamConfigurationMapBuilder}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class StreamConfigurationMapBuilderTest {
  @Test
  public void testGetOutputSizes() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addOutputSize(size1)
            .addOutputSize(size2)
            .build();
    assertThat(Arrays.asList(map.getOutputSizes(MediaRecorder.class)))
        .containsExactly(size1, size2);
  }
}

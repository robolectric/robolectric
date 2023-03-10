package org.robolectric.shadows;

import static android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;

import android.media.AudioDeviceInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link AudioDeviceInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class AudioDeviceInfoBuilderTest {

  @Test
  public void canCreateAudioDeviceInfoWithDesiredType() {
    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder().setType(TYPE_BLUETOOTH_A2DP).build();

    assertThat(audioDeviceInfo.getType()).isEqualTo(TYPE_BLUETOOTH_A2DP);
  }
}

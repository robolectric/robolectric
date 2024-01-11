package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link AudioDeviceInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class AudioDeviceInfoBuilderTest {

  @Config(minSdk = M, maxSdk = R)
  @Test
  public void buildAudioDeviceInfo_apiM_withDefaultValues_buildsExpectedObject() {
    AudioDeviceInfo audioDeviceInfo = AudioDeviceInfoBuilder.newBuilder().build();

    assertThat(audioDeviceInfo.getType()).isEqualTo(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
  }

  @Config(minSdk = S)
  @Test
  public void buildAudioDeviceInfo_apiS_withDefaultValues_buildsExpectedObject() {
    AudioDeviceInfo audioDeviceInfo = AudioDeviceInfoBuilder.newBuilder().build();

    assertThat(audioDeviceInfo.getType()).isEqualTo(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
    assertThat(audioDeviceInfo.getAudioProfiles()).isEmpty();
  }

  @Config(minSdk = M, maxSdk = R)
  @Test
  public void buildAudioDeviceInfo_apiM_witSetValues_buildsExpectedObject() {
    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP).build();

    assertThat(audioDeviceInfo.getType()).isEqualTo(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP);
  }

  @Test
  @Config(minSdk = S)
  public void buildAudioDeviceInfo_apiS_witSetValues_buildsExpectedObject() {
    ImmutableList<AudioProfile> audioProfiles =
        ImmutableList.of(
            AudioProfileBuilder.newBuilder().setFormat(AudioFormat.ENCODING_PCM_32BIT).build());
    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder()
            .setType(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
            .setProfiles(audioProfiles)
            .build();

    assertThat(audioDeviceInfo.getType()).isEqualTo(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP);
    assertThat(audioDeviceInfo.getAudioProfiles()).isEqualTo(audioProfiles);
  }
}

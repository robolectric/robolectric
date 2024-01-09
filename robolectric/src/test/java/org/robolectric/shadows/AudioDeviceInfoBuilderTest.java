package org.robolectric.shadows;

import static android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP;
import static android.media.AudioDeviceInfo.TYPE_HDMI;
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

  @Test
  @Config(minSdk = S)
  public void canCreateAudioDeviceInfoWithDesiredTypeAndProfiles() {
    AudioProfile expectedProfiles =
        AudioProfileBuilder.newBuilder()
            .setFormat(AudioFormat.ENCODING_AC3)
            .setSamplingRates(new int[] {48_000})
            .setChannelMasks(new int[] {AudioFormat.CHANNEL_OUT_5POINT1})
            .setEncapsulationType(AudioProfile.AUDIO_ENCAPSULATION_TYPE_NONE)
            .build();

    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder()
            .setType(TYPE_HDMI)
            .setProfiles(ImmutableList.of(expectedProfiles))
            .build();

    assertThat(audioDeviceInfo.getType()).isEqualTo(TYPE_HDMI);
    assertThat(audioDeviceInfo.getAudioProfiles()).containsExactly(expectedProfiles);
  }

  @Test
  @Config(maxSdk = R)
  public void canCreateAudioDeviceInfoWithDesiredType() {
    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder().setType(TYPE_BLUETOOTH_A2DP).build();

    assertThat(audioDeviceInfo.getType()).isEqualTo(TYPE_BLUETOOTH_A2DP);
  }
}

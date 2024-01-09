package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;

import android.media.AudioFormat;
import android.media.AudioProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link AudioProfileBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = S)
public class AudioProfileBuilderTest {

  @Test
  public void canCreateAudioProfile() {
    AudioProfile audioProfile =
        AudioProfileBuilder.newBuilder()
            .setFormat(AudioFormat.ENCODING_AC3)
            .setSamplingRates(new int[] {48_000})
            .setChannelMasks(new int[] {AudioFormat.CHANNEL_OUT_5POINT1})
            // The canonical channel index masks by channel count are given by the formula
            // (1 << channelCount) - 1. See:
            // https://developer.android.com/reference/android/media/AudioFormat#channelMask
            .setChannelIndexMasks(new int[] {0x3F})
            .setEncapsulationType(AudioProfile.AUDIO_ENCAPSULATION_TYPE_NONE)
            .build();

    assertThat(audioProfile.getFormat()).isEqualTo(AudioFormat.ENCODING_AC3);
    int[] sampleRates = audioProfile.getSampleRates();
    assertThat(sampleRates).hasLength(1);
    assertThat(sampleRates[0]).isEqualTo(48_000);
    int[] channelMasks = audioProfile.getChannelMasks();
    assertThat(channelMasks).hasLength(1);
    assertThat(channelMasks[0]).isEqualTo(AudioFormat.CHANNEL_OUT_5POINT1);
    int[] channelIndexMasks = audioProfile.getChannelIndexMasks();
    assertThat(channelIndexMasks).hasLength(1);
    assertThat(channelIndexMasks[0]).isEqualTo(0x3F);
    assertThat(audioProfile.getEncapsulationType())
        .isEqualTo(AudioProfile.AUDIO_ENCAPSULATION_TYPE_NONE);
  }
}

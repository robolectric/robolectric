package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.media.AudioAttributes;
import android.media.session.MediaController.PlaybackInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link PlaybackInfoBuilder} */
@RunWith(AndroidJUnit4.class)
public class PlaybackInfoBuilderTest {
  @Test
  public void build_playbackInfo() {
    PlaybackInfo playbackInfo =
        PlaybackInfoBuilder.newBuilder()
            .setVolumeType(PlaybackInfo.PLAYBACK_TYPE_LOCAL)
            .setVolumeControl(1)
            .setCurrentVolume(2)
            .setMaxVolume(3)
            .setAudioAttributes(new AudioAttributes.Builder().build())
            .build();

    assertThat(playbackInfo).isNotNull();
    assertThat(playbackInfo.getPlaybackType()).isEqualTo(PlaybackInfo.PLAYBACK_TYPE_LOCAL);
    assertThat(playbackInfo.getVolumeControl()).isEqualTo(1);
    assertThat(playbackInfo.getCurrentVolume()).isEqualTo(2);
    assertThat(playbackInfo.getMaxVolume()).isEqualTo(3);
    assertThat(playbackInfo.getAudioAttributes()).isEqualTo(new AudioAttributes.Builder().build());
  }
}

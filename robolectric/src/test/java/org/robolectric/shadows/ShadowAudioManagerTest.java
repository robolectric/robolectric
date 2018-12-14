package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowAudioManagerTest {
  private final AudioManager audioManager =
      new AudioManager((Application) ApplicationProvider.getApplicationContext());
  private final AudioManager.OnAudioFocusChangeListener listener = new AudioManager.OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {
    }
  };

  @Test
  public void requestAudioFocus_shouldRecordArgumentsOfMostRecentCall() {
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest()).isNull();
    audioManager.requestAudioFocus(listener, 999, 888);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().listener).isSameAs(listener);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().streamType).isEqualTo(999);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().durationHint).isEqualTo(888);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().audioFocusRequest).isNull();
  }

  @Test
  public void requestAudioFocus_shouldReturnTheSpecifiedValue() {
    int value = audioManager.requestAudioFocus(listener, 999, 888);
    assertThat(value).isEqualTo(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

    shadowOf(audioManager).setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

    value = audioManager.requestAudioFocus(listener, 999, 888);
    assertThat(value).isEqualTo(AudioManager.AUDIOFOCUS_REQUEST_FAILED);
  }

  @Test
  @Config(minSdk = O)
  public void requestAudioFocus2_shouldRecordArgumentsOfMostRecentCall() {
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest()).isNull();

    AudioAttributes atts = new AudioAttributes.Builder().build();
    android.media.AudioFocusRequest request =
        new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(atts)
            .build();

    audioManager.requestAudioFocus(request);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().listener).isNull();
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().streamType).isEqualTo(-1);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().durationHint).isEqualTo(-1);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().audioFocusRequest)
        .isEqualTo(request);
  }

  @Test
  @Config(minSdk = O)
  public void requestAudioFocus2_shouldReturnTheSpecifiedValue() {
    int value =
        audioManager.requestAudioFocus(
            new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build());
    assertThat(value).isEqualTo(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

    shadowOf(audioManager).setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

    value =
        audioManager.requestAudioFocus(
            new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build());
    assertThat(value).isEqualTo(AudioManager.AUDIOFOCUS_REQUEST_FAILED);
  }

  @Test
  public void abandonAudioFocus_shouldRecordTheListenerOfTheMostRecentCall() {
    audioManager.abandonAudioFocus(null);
    assertThat(shadowOf(audioManager).getLastAbandonedAudioFocusListener()).isNull();

    audioManager.abandonAudioFocus(listener);
    assertThat(shadowOf(audioManager).getLastAbandonedAudioFocusListener()).isSameAs(listener);
  }

  @Test
  @Config(minSdk = O)
  public void abandonAudioFocusRequest_shouldRecordTheListenerOfTheMostRecentCall() {
    android.media.AudioFocusRequest request =
        new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build();
    audioManager.abandonAudioFocusRequest(request);
    assertThat(shadowOf(audioManager).getLastAbandonedAudioFocusRequest()).isSameAs(request);
  }

  @Test
  public void getStreamMaxVolume_shouldReturnMaxVolume() throws Exception {
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      switch(stream) {
        case AudioManager.STREAM_MUSIC:
        case AudioManager.STREAM_DTMF:
          assertThat(audioManager.getStreamMaxVolume(stream))
              .isEqualTo(ShadowAudioManager.MAX_VOLUME_MUSIC_DTMF);
          break;

        case AudioManager.STREAM_ALARM:
        case AudioManager.STREAM_NOTIFICATION:
        case AudioManager.STREAM_RING:
        case AudioManager.STREAM_SYSTEM:
        case AudioManager.STREAM_VOICE_CALL:
          assertThat(audioManager.getStreamMaxVolume(stream))
              .isEqualTo(ShadowAudioManager.DEFAULT_MAX_VOLUME);
          break;

        default:
          throw new Exception("Unexpected audio stream requested.");
      }
    }
  }

  @Test
  public void setStreamVolume_shouldSetVolume() {
    int vol = 1;
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      audioManager.setStreamVolume(stream, vol, 0);
      vol++;
      if (vol > ShadowAudioManager.DEFAULT_MAX_VOLUME) {
        vol = 1;
      }
    }

    vol = 1;
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(audioManager.getStreamVolume(stream)).isEqualTo(vol);
      vol++;
      if (vol > ShadowAudioManager.DEFAULT_MAX_VOLUME) {
        vol = 1;
      }
    }
  }

  @Test
  public void setStreamMaxVolume_shouldSetMaxVolumeForAllStreams() {
    final int newMaxVol = 31;
    shadowOf(audioManager).setStreamMaxVolume(newMaxVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(audioManager.getStreamMaxVolume(stream)).isEqualTo(newMaxVol);
    }
  }

  @Test
  public void setStreamVolume_shouldSetVolumeForAllStreams() {
    final int newVol = 3;
    shadowOf(audioManager).setStreamVolume(newVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(audioManager.getStreamVolume(stream)).isEqualTo(newVol);
    }
  }

  @Test
  public void setStreamVolume_shouldNotAllowNegativeValues() {
    final int newVol = -3;
    shadowOf(audioManager).setStreamVolume(newVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(audioManager.getStreamVolume(stream)).isEqualTo(0);
    }
  }

  @Test
  public void setStreamVolume_shouldNotExceedMaxVolume() throws Exception {
    final int newVol = 31;
    shadowOf(audioManager).setStreamVolume(newVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      switch(stream) {
        case AudioManager.STREAM_MUSIC:
        case AudioManager.STREAM_DTMF:
          assertThat(audioManager.getStreamMaxVolume(stream))
              .isEqualTo(ShadowAudioManager.MAX_VOLUME_MUSIC_DTMF);
          break;

        case AudioManager.STREAM_ALARM:
        case AudioManager.STREAM_NOTIFICATION:
        case AudioManager.STREAM_RING:
        case AudioManager.STREAM_SYSTEM:
        case AudioManager.STREAM_VOICE_CALL:
          assertThat(audioManager.getStreamMaxVolume(stream))
              .isEqualTo(ShadowAudioManager.DEFAULT_MAX_VOLUME);
          break;

        default:
          throw new Exception("Unexpected audio stream requested.");
      }
    }
  }

  @Test
  public void getRingerMode_default() {
    int ringerMode = audioManager.getRingerMode();
    assertThat(ringerMode).isEqualTo(AudioManager.RINGER_MODE_NORMAL);
  }

  @Test
  public void setRingerMode_shouldSetMode() {
    for (int rm = AudioManager.RINGER_MODE_SILENT; rm <= AudioManager.RINGER_MODE_NORMAL; rm++) {
      audioManager.setRingerMode(rm);
      assertThat(audioManager.getRingerMode()).isEqualTo(rm);
    }
  }

  @Test
  public void setRingerMode_shouldNotChangeOnInvalidValue() {
    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    assertThat(audioManager.getRingerMode()).isEqualTo(AudioManager.RINGER_MODE_VIBRATE);
    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL + 1);
    assertThat(audioManager.getRingerMode()).isEqualTo(AudioManager.RINGER_MODE_VIBRATE);
  }

  @Test
  public void getMode_default() {
    assertThat(audioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
  }

  @Test
  public void setMode_shouldSetAudioMode() {
    audioManager.setMode(AudioManager.MODE_RINGTONE);
    assertThat(audioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);
  }

  @Test
  public void isSpeakerphoneOn_shouldReturnSpeakerphoneState() {
    assertThat(audioManager.isSpeakerphoneOn()).isFalse();
    audioManager.setSpeakerphoneOn(true);
    assertThat(audioManager.isSpeakerphoneOn()).isTrue();
  }

  @Test
  public void microphoneShouldMute() {
    //Should not be muted by default
    assertThat(audioManager.isMicrophoneMute()).isFalse();
    audioManager.setMicrophoneMute(true);
    assertThat(audioManager.isMicrophoneMute()).isTrue();
  }

  @Test
  public void setBluetoothScoOn() {
    assertThat(audioManager.isBluetoothScoOn()).isFalse();
    audioManager.setBluetoothScoOn(true);
    assertThat(audioManager.isBluetoothScoOn()).isTrue();
  }

  @Test
  public void isMusicActive() {
    assertThat(audioManager.isMusicActive()).isFalse();
    shadowOf(audioManager).setIsMusicActive(true);
    assertThat(audioManager.isMusicActive()).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void getActivePlaybackConfigurations() {
    assertThat(audioManager.getActivePlaybackConfigurations()).isEmpty();
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    AudioAttributes musicAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
    shadowOf(audioManager)
        .setActivePlaybackConfigurationsFor(
            Arrays.asList(
                new AudioAttributes[] {
                  movieAttribute, musicAttribute,
                }));
    List<AudioPlaybackConfiguration> playbackConfigurations =
        audioManager.getActivePlaybackConfigurations();
    assertThat(playbackConfigurations).hasSize(2);
    assertThat(playbackConfigurations.get(0).getAudioAttributes()).isEqualTo(movieAttribute);
    assertThat(playbackConfigurations.get(1).getAudioAttributes()).isEqualTo(musicAttribute);
  }
}

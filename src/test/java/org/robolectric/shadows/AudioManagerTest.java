package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import android.media.AudioManager;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AudioManagerTest {
  private final AudioManager audioManager = new AudioManager(Robolectric.application);
  private final ShadowAudioManager shadowAudioManager = Robolectric.shadowOf(audioManager);
  private final AudioManager.OnAudioFocusChangeListener listener = new AudioManager.OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {
    }
  };

  @Test
  public void requestAudioFocus_shouldRecordArgumentsOfMostRecentCall() {
    assertThat(shadowAudioManager.getLastAudioFocusRequest()).isNull();
    audioManager.requestAudioFocus(listener, 999, 888);
    assertThat(shadowAudioManager.getLastAudioFocusRequest().listener).isSameAs(listener);
    assertThat(shadowAudioManager.getLastAudioFocusRequest().streamType).isEqualTo(999);
    assertThat(shadowAudioManager.getLastAudioFocusRequest().durationHint).isEqualTo(888);
  }

  @Test
  public void requestAudioFocus_shouldReturnTheSpecifiedValue() {
    int value = audioManager.requestAudioFocus(listener, 999, 888);
    assertThat(AudioManager.AUDIOFOCUS_REQUEST_GRANTED).isEqualTo(value);

    shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

    value = audioManager.requestAudioFocus(listener, 999, 888);
    assertThat(AudioManager.AUDIOFOCUS_REQUEST_FAILED).isEqualTo(value);
  }

  @Test
  public void abandonAudioFocus_shouldRecordTheListenerOfTheMostRecentCall() {
    audioManager.abandonAudioFocus(null);
    assertThat(shadowAudioManager.getLastAbandonedAudioFocusListener()).isNull();

    audioManager.abandonAudioFocus(listener);
    assertThat(shadowAudioManager.getLastAbandonedAudioFocusListener()).isSameAs(listener);
  }

  @Test
  public void getStreamMaxVolume_shouldReturnMaxVolume() throws Exception {
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      switch(stream) {
        case AudioManager.STREAM_MUSIC:
        case AudioManager.STREAM_DTMF:
          assertThat(shadowAudioManager.getStreamMaxVolume(stream)).isEqualTo(ShadowAudioManager.MAX_VOLUME_MUSIC_DTMF);
          break;

        case AudioManager.STREAM_ALARM:
        case AudioManager.STREAM_NOTIFICATION:
        case AudioManager.STREAM_RING:
        case AudioManager.STREAM_SYSTEM:
        case AudioManager.STREAM_VOICE_CALL:
          assertThat(shadowAudioManager.getStreamMaxVolume(stream)).isEqualTo(ShadowAudioManager.DEFAULT_MAX_VOLUME);
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
      shadowAudioManager.setStreamVolume(stream, vol, 0);
      vol++;
      if (vol > ShadowAudioManager.DEFAULT_MAX_VOLUME) {
        vol = 1;
      }
    }

    vol = 1;
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(shadowAudioManager.getStreamVolume(stream)).isEqualTo(vol);
      vol++;
      if (vol > ShadowAudioManager.DEFAULT_MAX_VOLUME) {
        vol = 1;
      }
    }
  }

  @Test
  public void setStreamMaxVolume_shouldSetMaxVolumeForAllStreams() {
    final int newMaxVol = 31;
    shadowAudioManager.setStreamMaxVolume(newMaxVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(shadowAudioManager.getStreamMaxVolume(stream)).isEqualTo(newMaxVol);
    }
  }

  @Test
  public void setStreamVolume_shouldSetVolumeForAllStreams() {
    final int newVol = 3;
    shadowAudioManager.setStreamVolume(newVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(shadowAudioManager.getStreamVolume(stream)).isEqualTo(newVol);
    }
  }

  @Test
  public void setStreamVolume_shouldNotAllowNegativeValues() {
    final int newVol = -3;
    shadowAudioManager.setStreamVolume(newVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      assertThat(shadowAudioManager.getStreamVolume(stream)).isZero();
    }
  }

  @Test
  public void setStreamVolume_shouldNotExceedMaxVolume() throws Exception {
    final int newVol = 31;
    shadowAudioManager.setStreamVolume(newVol);
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      switch(stream) {
        case AudioManager.STREAM_MUSIC:
        case AudioManager.STREAM_DTMF:
          assertThat(shadowAudioManager.getStreamMaxVolume(stream)).isEqualTo(ShadowAudioManager.MAX_VOLUME_MUSIC_DTMF);
          break;

        case AudioManager.STREAM_ALARM:
        case AudioManager.STREAM_NOTIFICATION:
        case AudioManager.STREAM_RING:
        case AudioManager.STREAM_SYSTEM:
        case AudioManager.STREAM_VOICE_CALL:
          assertThat(shadowAudioManager.getStreamMaxVolume(stream)).isEqualTo(ShadowAudioManager.DEFAULT_MAX_VOLUME);
          break;

        default:
          throw new Exception("Unexpected audio stream requested.");
      }
    }
  }
}

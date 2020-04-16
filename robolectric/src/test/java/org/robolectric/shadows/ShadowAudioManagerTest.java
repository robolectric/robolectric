package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRecordingConfiguration;
import android.media.MediaRecorder.AudioSource;
import android.media.audiopolicy.AudioPolicy;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowAudioManagerTest {
  private final AudioManager.OnAudioFocusChangeListener listener =
      new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {}
      };

  private Context appContext;
  private AudioManager audioManager;

  @Before
  public void setUp() {
    appContext = ApplicationProvider.getApplicationContext();
    audioManager = new AudioManager((Application) appContext);
  }

  @Test
  public void requestAudioFocus_shouldRecordArgumentsOfMostRecentCall() {
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest()).isNull();
    audioManager.requestAudioFocus(listener, 999, 888);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().listener)
        .isSameInstanceAs(listener);
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
    assertThat(shadowOf(audioManager).getLastAbandonedAudioFocusListener())
        .isSameInstanceAs(listener);
  }

  @Test
  @Config(minSdk = O)
  public void abandonAudioFocusRequest_shouldRecordTheListenerOfTheMostRecentCall() {
    android.media.AudioFocusRequest request =
        new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build();
    audioManager.abandonAudioFocusRequest(request);
    assertThat(shadowOf(audioManager).getLastAbandonedAudioFocusRequest())
        .isSameInstanceAs(request);
  }

  @Test
  public void getStreamMaxVolume_shouldReturnMaxVolume() throws Exception {
    for (int stream : ShadowAudioManager.ALL_STREAMS) {
      switch (stream) {
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
      switch (stream) {
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
    // Should not be muted by default
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
  public void isBluetoothScoAvailableOffCall() {
    assertThat(audioManager.isBluetoothScoAvailableOffCall()).isFalse();
    shadowOf(audioManager).setIsBluetoothScoAvailableOffCall(true);
    assertThat(audioManager.isBluetoothScoAvailableOffCall()).isTrue();
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

  @Test
  @Config(minSdk = O)
  public void setActivePlaybackConfigurations_withCallbackRegistered_notifiesCallback() {
    AudioManager.AudioPlaybackCallback callback = mock(AudioManager.AudioPlaybackCallback.class);
    audioManager.registerAudioPlaybackCallback(callback, null);

    List<AudioAttributes> audioAttributes = new ArrayList<>();
    shadowOf(audioManager).setActivePlaybackConfigurationsFor(audioAttributes, true);

    verify(callback).onPlaybackConfigChanged(any());
  }

  @Test
  @Config(minSdk = O)
  public void unregisterAudioPlaybackCallback_removesCallback() {
    AudioManager.AudioPlaybackCallback callback = mock(AudioManager.AudioPlaybackCallback.class);
    audioManager.registerAudioPlaybackCallback(callback, null);

    audioManager.unregisterAudioPlaybackCallback(callback);
    List<AudioAttributes> audioAttributes = new ArrayList<>();
    shadowOf(audioManager).setActivePlaybackConfigurationsFor(audioAttributes, true);

    verifyZeroInteractions(callback);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setParameters_mustNotBeEmpty() {
    audioManager.setParameters("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setParameters_mustEndInSemicolon() {
    audioManager.setParameters("foo=bar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setParameters_mustHaveEquals() {
    audioManager.setParameters("foobar;");
  }

  @Test(expected = IllegalArgumentException.class)
  public void setParameters_crazyInput() {
    audioManager.setParameters("foo=bar=baz;");
  }

  @Test
  public void setParameters() {
    audioManager.setParameters("foo=bar;");
    assertThat(shadowOf(audioManager).getParameter("foo")).isEqualTo("bar");
  }

  @Test
  public void getParameters() {
    assertThat(audioManager.getParameters("")).isNull();
  }

  @Test
  public void setParameters_multipleParametersOk() {
    audioManager.setParameters("foo=bar;baz=bar;");
    assertThat(shadowOf(audioManager).getParameter("foo")).isEqualTo("bar");
    assertThat(shadowOf(audioManager).getParameter("baz")).isEqualTo("bar");
  }

  @Test
  @Config(minSdk = M)
  public void adjustStreamVolume_mute() {
    assertThat(audioManager.isStreamMute(AudioManager.STREAM_VOICE_CALL)).isFalse();

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_MUTE, /* flags= */ 0);
    assertThat(audioManager.isStreamMute(AudioManager.STREAM_VOICE_CALL)).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void adjustStreamVolume_unmute() {
    audioManager.adjustStreamVolume(
        AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_MUTE, /* flags= */ 0);
    audioManager.adjustStreamVolume(
        AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_UNMUTE, /* flags= */ 0);

    assertThat(audioManager.isStreamMute(AudioManager.STREAM_VOICE_CALL)).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void isStreamMute_defaultFalse() {
    assertThat(audioManager.isStreamMute(AudioManager.STREAM_VOICE_CALL)).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void getActiveRecordingConfigurations_defaultEmptyList() {
    assertThat(audioManager.getActiveRecordingConfigurations()).isEmpty();
  }

  @Test
  @Config(minSdk = N)
  public void getActiveRecordingConfigurations_returnsSpecifiedList() {
    ArrayList<AudioRecordingConfiguration> configurations = new ArrayList<>();
    configurations.add(
        shadowOf(audioManager)
            .createActiveRecordingConfiguration(
                0, AudioSource.VOICE_RECOGNITION, "com.example.android.application"));
    shadowOf(audioManager).setActiveRecordingConfigurations(configurations, true);

    assertThat(audioManager.getActiveRecordingConfigurations()).isEqualTo(configurations);
  }

  @Test
  @Config(minSdk = N)
  public void setActiveRecordingConfigurations_notifiesCallback() {
    AudioManager.AudioRecordingCallback callback = mock(AudioManager.AudioRecordingCallback.class);
    audioManager.registerAudioRecordingCallback(callback, null);

    ArrayList<AudioRecordingConfiguration> configurations = new ArrayList<>();
    configurations.add(
        shadowOf(audioManager)
            .createActiveRecordingConfiguration(
                0, AudioSource.VOICE_RECOGNITION, "com.example.android.application"));
    shadowOf(audioManager).setActiveRecordingConfigurations(configurations, true);

    verify(callback).onRecordingConfigChanged(configurations);
  }

  @Test
  @Config(minSdk = N)
  public void unregisterAudioRecordingCallback_removesCallback() {
    AudioManager.AudioRecordingCallback callback = mock(AudioManager.AudioRecordingCallback.class);
    audioManager.registerAudioRecordingCallback(callback, null);

    audioManager.unregisterAudioRecordingCallback(callback);

    ArrayList<AudioRecordingConfiguration> configurations = new ArrayList<>();
    configurations.add(
        shadowOf(audioManager)
            .createActiveRecordingConfiguration(
                0, AudioSource.VOICE_RECOGNITION, "com.example.android.application"));
    shadowOf(audioManager).setActiveRecordingConfigurations(configurations, true);

    verifyZeroInteractions(callback);
  }

  @Test
  @Config(minSdk = N)
  public void createActiveRecordingConfiguration_createsProperConfiguration() {
    AudioRecordingConfiguration configuration =
        shadowOf(audioManager)
            .createActiveRecordingConfiguration(
                12345, AudioSource.VOICE_RECOGNITION, "com.example.android.application");

    assertThat(configuration.getClientAudioSessionId()).isEqualTo(12345);
    assertThat(configuration.getClientAudioSource()).isEqualTo(AudioSource.VOICE_RECOGNITION);
    assertThat(configuration.getClientFormat().getEncoding())
        .isEqualTo(AudioFormat.ENCODING_PCM_16BIT);
    assertThat(configuration.getClientFormat().getSampleRate()).isEqualTo(16000);
    assertThat(configuration.getClientFormat().getChannelMask())
        .isEqualTo(AudioFormat.CHANNEL_OUT_MONO);
    assertThat(configuration.getFormat().getEncoding()).isEqualTo(AudioFormat.ENCODING_PCM_16BIT);
    assertThat(configuration.getFormat().getSampleRate()).isEqualTo(16000);
    assertThat(configuration.getFormat().getChannelMask()).isEqualTo(AudioFormat.CHANNEL_OUT_MONO);
  }

  @Test(expected = NullPointerException.class)
  @Config(minSdk = P)
  public void registerAudioPolicy_nullAudioPolicy_throwsException() {
    audioManager.registerAudioPolicy(null);
  }

  @Test
  @Config(minSdk = P)
  public void registerAudioPolicy_alreadyRegistered_returnsError() {
    AudioPolicy audioPolicy = new AudioPolicy.Builder(appContext).build();
    audioManager.registerAudioPolicy(audioPolicy);

    assertThat(audioManager.registerAudioPolicy(audioPolicy)).isEqualTo(AudioManager.ERROR);
  }

  @Test
  @Config(minSdk = P)
  public void registerAudioPolicy_noPreviouslyRegistered_returnsSuccess() {
    AudioPolicy audioPolicy = new AudioPolicy.Builder(appContext).build();

    assertThat(audioManager.registerAudioPolicy(audioPolicy)).isEqualTo(AudioManager.SUCCESS);
  }

  @Test
  @Config(minSdk = P)
  public void isAnyAudioPolicyRegistered_noPoliciesRegistered_returnsFalse() {
    assertThat(shadowOf(audioManager).isAnyAudioPolicyRegistered()).isFalse();
  }

  @Test
  @Config(minSdk = P)
  public void isAnyAudioPolicyRegistered_afterPolicyRegistered_returnsTrue() {
    AudioPolicy audioPolicy = new AudioPolicy.Builder(appContext).build();

    audioManager.registerAudioPolicy(audioPolicy);

    assertThat(shadowOf(audioManager).isAnyAudioPolicyRegistered()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void isAnyAudioPolicyRegistered_afterPolicyRegisteredAndUnregistered_returnsFalse() {
    AudioPolicy audioPolicy = new AudioPolicy.Builder(appContext).build();

    audioManager.registerAudioPolicy(audioPolicy);
    audioManager.unregisterAudioPolicy(audioPolicy);

    assertThat(shadowOf(audioManager).isAnyAudioPolicyRegistered()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void generateAudioSessionId_returnsPositiveValues() {
    int audioSessionId = audioManager.generateAudioSessionId();
    int audioSessionId2 = audioManager.generateAudioSessionId();

    assertThat(audioSessionId).isGreaterThan(0);
    assertThat(audioSessionId2).isGreaterThan(0);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void generateAudioSessionId_returnsDistinctValues() {
    int audioSessionId = audioManager.generateAudioSessionId();
    int audioSessionId2 = audioManager.generateAudioSessionId();

    assertThat(audioSessionId).isNotEqualTo(audioSessionId2);
  }
}

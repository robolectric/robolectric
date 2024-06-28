package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnModeChangedListener;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioProfile;
import android.media.AudioRecordingConfiguration;
import android.media.AudioSystem;
import android.media.MediaRecorder.AudioSource;
import android.media.audiopolicy.AudioPolicy;
import android.view.KeyEvent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowAudioManagerTest {
  private static final float FAULT_TOLERANCE = 0.00001f;
  private final AudioManager.OnAudioFocusChangeListener listener = focusChange -> {};
  private final LocalOnModeChangedListener modeChangedListener = new LocalOnModeChangedListener();

  private Context appContext;
  private AudioManager audioManager;

  // When creating Audio Device Info, we need to pass external device type instead of internal input
  // device(e.g. AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
  // The mapping between external device type and internal input device is:
  // http://shortn/_7pV0nML4Cr
  // Copied from
  // http://cs/android-internal/frameworks/base/media/java/android/media/AudioSystem.java;l=989
  private static final int DEVICE_OUT_BLUETOOTH_SCO = 0x10;
  // Copied from
  // http://cs/android-internal/frameworks/base/media/java/android/media/AudioSystem.java;l=1000
  private static final int DEVICE_OUT_BLUETOOTH_A2DP = 0x80;

  @Before
  public void setUp() {
    appContext = ApplicationProvider.getApplicationContext();
    audioManager = new AudioManager(appContext);
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

    AudioAttributes atts =
        new AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
    android.media.AudioFocusRequest request =
        new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(listener)
            .setAudioAttributes(atts)
            .build();

    audioManager.requestAudioFocus(request);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().listener)
        .isSameInstanceAs(listener);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().streamType)
        .isEqualTo(AudioManager.STREAM_MUSIC);
    assertThat(shadowOf(audioManager).getLastAudioFocusRequest().durationHint)
        .isEqualTo(AudioManager.AUDIOFOCUS_GAIN);
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
        new android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(listener)
            .build();
    audioManager.abandonAudioFocusRequest(request);
    assertThat(shadowOf(audioManager).getLastAbandonedAudioFocusRequest())
        .isSameInstanceAs(request);
    assertThat(shadowOf(audioManager).getLastAbandonedAudioFocusListener())
        .isSameInstanceAs(listener);
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
        case AudioManager.STREAM_ACCESSIBILITY:
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
          assertThat(audioManager.getStreamVolume(stream))
              .isEqualTo(ShadowAudioManager.MAX_VOLUME_MUSIC_DTMF);
          break;

        case AudioManager.STREAM_ALARM:
        case AudioManager.STREAM_NOTIFICATION:
        case AudioManager.STREAM_RING:
        case AudioManager.STREAM_SYSTEM:
        case AudioManager.STREAM_VOICE_CALL:
        case AudioManager.STREAM_ACCESSIBILITY:
          assertThat(audioManager.getStreamVolume(stream))
              .isEqualTo(ShadowAudioManager.DEFAULT_MAX_VOLUME);
          break;

        default:
          throw new Exception("Unexpected audio stream requested.");
      }
    }
  }

  @Test
  @Config(minSdk = P)
  public void getStreamVolumeDb_maxVolume_returnsZero() {
    float volumeDb =
        audioManager.getStreamVolumeDb(
            AudioManager.STREAM_MUSIC,
            ShadowAudioManager.MAX_VOLUME_MUSIC_DTMF,
            /* deviceType= */ 0);

    assertThat(volumeDb).isWithin(FAULT_TOLERANCE).of(0);
  }

  @Test
  @Config(minSdk = P)
  public void getStreamVolumeDb_minVolume_returnsNegativeInf() {
    float volumeDb =
        audioManager.getStreamVolumeDb(
            AudioManager.STREAM_MUSIC, ShadowAudioManager.MIN_VOLUME, /* deviceType= */ 0);

    assertThat(volumeDb).isNegativeInfinity();
  }

  @Test
  @Config(minSdk = P)
  public void getStreamVolumeDb_mediumVolumes_returnsDecrementingNegativeValues() {
    int maxVolume = ShadowAudioManager.MAX_VOLUME_MUSIC_DTMF;
    int minVolume = ShadowAudioManager.MIN_VOLUME;
    float lastVolumeDb =
        audioManager.getStreamVolumeDb(AudioManager.STREAM_MUSIC, maxVolume, /* deviceType= */ 0);

    for (int volume = maxVolume - 1; volume > minVolume; volume--) {
      float volumeDb =
          audioManager.getStreamVolumeDb(AudioManager.STREAM_MUSIC, volume, /* deviceType= */ 0);

      assertThat(volumeDb).isLessThan(0);
      assertThat(volumeDb).isLessThan(lastVolumeDb);
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

  @Config(minSdk = S)
  @Test
  public void setModeNormal_listenerAdded_noNotification() {
    audioManager.addOnModeChangedListener(directExecutor(), modeChangedListener);

    audioManager.setMode(AudioManager.MODE_NORMAL);

    assertThat(modeChangedListener.modes).isEmpty();
  }

  @Config(minSdk = S)
  @Test
  public void setModeInCallAndBackNormal_listenerAdded_notification() {
    audioManager.addOnModeChangedListener(directExecutor(), modeChangedListener);

    audioManager.setMode(AudioManager.MODE_IN_CALL);
    audioManager.setMode(AudioManager.MODE_NORMAL);

    assertThat(modeChangedListener.modes)
        .containsExactly(AudioManager.MODE_IN_CALL, AudioManager.MODE_NORMAL)
        .inOrder();
  }

  @Config(minSdk = S)
  @Test
  public void addOnModeChangedListener_alreadyInCall_noInitialNotification() {
    audioManager.setMode(AudioManager.MODE_IN_CALL);

    audioManager.addOnModeChangedListener(directExecutor(), modeChangedListener);

    assertThat(modeChangedListener.modes).isEmpty();
  }

  @Config(minSdk = S)
  @Test
  public void removeOnModeChangedListenerAndSetModeInCall_listenerAdded_noNotification() {
    audioManager.addOnModeChangedListener(directExecutor(), modeChangedListener);

    audioManager.removeOnModeChangedListener(modeChangedListener);
    audioManager.setMode(AudioManager.MODE_IN_CALL);

    assertThat(modeChangedListener.modes).isEmpty();
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
  public void lockMode_locked_modeRemainsTheSame() {
    shadowOf(audioManager).lockMode(true);

    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

    assertThat(audioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
  }

  @Test
  public void lockMode_notLocked_modeIsSet() {
    shadowOf(audioManager).lockMode(false);

    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

    assertThat(audioManager.getMode()).isEqualTo(AudioManager.MODE_IN_COMMUNICATION);
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
  @Config(minSdk = R)
  public void getDevicesForAttributes_returnsEmptyListByDefault() {
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();

    assertThat(shadowOf(audioManager).getDevicesForAttributes(movieAttribute)).isEmpty();
  }

  @Test
  @Config(minSdk = R)
  public void setDevicesForAttributes_updatesDevicesForAttributes() {
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    ImmutableList<Object> newDevices = ImmutableList.of(new Object());

    shadowOf(audioManager).setDevicesForAttributes(movieAttribute, newDevices);

    assertThat(shadowOf(audioManager).getDevicesForAttributes(movieAttribute))
        .isEqualTo(newDevices);
  }

  @Test
  @Config(minSdk = R)
  public void setDefaultDevicesForAttributes_updatesDevicesForAttributes() {
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    ImmutableList<Object> newDevices = ImmutableList.of(new Object());

    shadowOf(audioManager).setDefaultDevicesForAttributes(newDevices);

    assertThat(shadowOf(audioManager).getDevicesForAttributes(movieAttribute))
        .isEqualTo(newDevices);
  }

  @Test
  @Config(minSdk = R)
  public void setDevicesForAttributes_overridesSetDefaultDevicesForAttributes() {
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    shadowOf(audioManager).setDefaultDevicesForAttributes(ImmutableList.of(new Object()));
    ImmutableList<Object> newDevices = ImmutableList.of(new Object(), new Object());

    shadowOf(audioManager).setDevicesForAttributes(movieAttribute, newDevices);

    assertThat(shadowOf(audioManager).getDevicesForAttributes(movieAttribute))
        .isEqualTo(newDevices);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getAudioDevicesForAttributes_returnsEmptyListByDefault() {
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();

    assertThat(audioManager.getAudioDevicesForAttributes(movieAttribute)).isEmpty();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setAudioDevicesForAttributes_updatesAudioDevicesForAttributes() {
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    ImmutableList<AudioDeviceInfo> newDevices =
        ImmutableList.of(
            AudioDeviceInfoBuilder.newBuilder()
                .setType(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                .build());

    shadowOf(audioManager).setAudioDevicesForAttributes(movieAttribute, newDevices);

    assertThat(audioManager.getAudioDevicesForAttributes(movieAttribute)).isEqualTo(newDevices);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setAudioDevicesForAttributes_returnsEmptyListForOtherAttributes() {
    AudioAttributes movieAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    AudioAttributes otherAttribute =
        new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
    ImmutableList<AudioDeviceInfo> newDevices =
        ImmutableList.of(
            AudioDeviceInfoBuilder.newBuilder()
                .setType(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                .build());

    shadowOf(audioManager).setAudioDevicesForAttributes(movieAttribute, newDevices);

    assertThat(audioManager.getAudioDevicesForAttributes(otherAttribute)).isEmpty();
  }

  @Test
  @Config(minSdk = M)
  public void registerAudioDeviceCallback_availableDevices_onAudioDevicesAddedCallback()
      throws Exception {
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setInputDevices(Collections.singletonList(device));

    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);

    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {device});
  }

  @Test
  @Config(minSdk = M)
  public void setInputDevices_withCallbackRegistered_noNotificationCallback() throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setInputDevices(Collections.singletonList(device));

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void addInputDevice_callbackRegisteredUnregistered_noNotificationCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    audioManager.unregisterAudioDeviceCallback(callback);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).addInputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void addInputDevice_withCallbackRegisteredAndNoDevice_deviceAddedAndNotifiesCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).addInputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {device});
  }

  @Test
  @Config(minSdk = M)
  public void
      addInputDeviceNoCallbackNotification_withCallbackRegisteredAndNoDevice_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).addInputDevice(device, /* notifyAudioDeviceCallbacks= */ false);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void addInputDevice_withCallbackRegisteredAndDevicePresent_noNotificationCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setInputDevices(Collections.singletonList(device));

    shadowOf(audioManager).addInputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void
      removeInputDevice_withCallbackRegisteredAndDevicePresent_deviceRemovedAndNotifiesCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setInputDevices(Collections.singletonList(device));

    shadowOf(audioManager).removeInputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verify(callback).onAudioDevicesRemoved(new AudioDeviceInfo[] {device});
  }

  @Test
  @Config(minSdk = M)
  public void
      removeInputDeviceNoCallbackNotification_withCallbackRegisteredAndDevicePresent_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setInputDevices(Collections.singletonList(device));

    shadowOf(audioManager).removeInputDevice(device, /* notifyAudioDeviceCallbacks= */ false);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void removeInputDevice_withCallbackRegisteredAndNoDevice_noNotificationCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).removeInputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void setOutputDevices_withCallbackRegistered_noNotificationCallback() throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setOutputDevices(Collections.singletonList(device));

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void addOutputDevice_withCallbackRegisteredAndNoDevice_deviceAddedAndNotifiesCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).addOutputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {device});
  }

  @Test
  @Config(minSdk = M)
  public void
      addOutputDeviceNoCallbackNotification_withCallbackRegisteredAndNoDevice_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).addOutputDevice(device, /* notifyAudioDeviceCallbacks= */ false);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void addOutputDevice_withCallbackRegisteredAndDevicePresent_noNotificationCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setOutputDevices(Collections.singletonList(device));

    shadowOf(audioManager).addOutputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void
      removeOutputDevice_withCallbackRegisteredAndDevicePresent_deviceRemovedAndNotifiesCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setOutputDevices(Collections.singletonList(device));

    shadowOf(audioManager).removeOutputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verify(callback).onAudioDevicesRemoved(new AudioDeviceInfo[] {device});
  }

  @Test
  @Config(minSdk = M)
  public void
      removeOutputDeviceNoCallbackNotification_withCallbackRegisteredAndDevicePresent_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setOutputDevices(Collections.singletonList(device));

    shadowOf(audioManager).removeOutputDevice(device, /* notifyAudioDeviceCallbacks= */ false);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void removeOutputDevice_withCallbackRegisteredAndNoDevice_noNotificationCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).removeOutputDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = S)
  public void setAvailableCommunicationDevices_withCallbackRegistered_noNotificationCallback()
      throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setAvailableCommunicationDevices(Collections.singletonList(device));

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = S)
  public void
      addAvailableCommunicationDevice_withCallbackRegisteredAndNoDevice_deviceAddedAndNotifiesCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager)
        .addAvailableCommunicationDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {device});
  }

  @Test
  @Config(minSdk = S)
  public void
      addAvailableCommunicationDeviceNoCallbackNotification_withCallbackRegisteredAndNoDevice_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager)
        .addAvailableCommunicationDevice(device, /* notifyAudioDeviceCallbacks= */ false);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = S)
  public void
      addAvailableCommunicationDevice_withCallbackRegisteredAndDevicePresent_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setAvailableCommunicationDevices(Collections.singletonList(device));

    shadowOf(audioManager)
        .addAvailableCommunicationDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = S)
  public void
      removeAvailableCommunicationDevice_withCallbackRegisteredAndDevicePresent_deviceRemovedAndNotifiesCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setAvailableCommunicationDevices(Collections.singletonList(device));

    shadowOf(audioManager)
        .removeAvailableCommunicationDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verify(callback).onAudioDevicesRemoved(new AudioDeviceInfo[] {device});
  }

  @Test
  @Config(minSdk = S)
  public void
      removeAvailableCommunicationDeviceNoCallbackNotification_withCallbackRegisteredAndDevicePresent_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).setAvailableCommunicationDevices(Collections.singletonList(device));

    shadowOf(audioManager)
        .removeAvailableCommunicationDevice(device, /* notifyAudioDeviceCallbacks= */ false);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = S)
  public void
      removeAvailableCommunicationDevice_withCallbackRegisteredAndNoDevice_noNotificationCallback()
          throws Exception {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration

    AudioDeviceInfo device = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager)
        .removeAvailableCommunicationDevice(device, /* notifyAudioDeviceCallbacks= */ true);

    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = M)
  public void getDevices_criteriaInputs_getsAllInputDevices() throws Exception {
    AudioDeviceInfo scoDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    AudioDeviceInfo a2dpDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_A2DP);
    shadowOf(audioManager).setInputDevices(ImmutableList.of(scoDevice));
    shadowOf(audioManager).setOutputDevices(ImmutableList.of(a2dpDevice));

    assertThat(Arrays.stream(shadowOf(audioManager).getDevices(AudioManager.GET_DEVICES_INPUTS)))
        .containsExactly(scoDevice);
  }

  @Test
  @Config(minSdk = M)
  public void getDevices_criteriaOutputs_getsAllOutputDevices() throws Exception {
    AudioDeviceInfo scoDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    AudioDeviceInfo a2dpDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_A2DP);
    shadowOf(audioManager).setInputDevices(ImmutableList.of(scoDevice));
    shadowOf(audioManager).setOutputDevices(ImmutableList.of(a2dpDevice));

    assertThat(Arrays.stream(shadowOf(audioManager).getDevices(AudioManager.GET_DEVICES_OUTPUTS)))
        .containsExactly(a2dpDevice);
  }

  @Test
  @Config(minSdk = M)
  public void getDevices_criteriaInputsAndOutputs_getsAllDevices() throws Exception {
    AudioDeviceInfo scoDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    AudioDeviceInfo a2dpDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_A2DP);
    shadowOf(audioManager).setInputDevices(ImmutableList.of(scoDevice));
    shadowOf(audioManager).setOutputDevices(ImmutableList.of(a2dpDevice));

    assertThat(Arrays.stream(shadowOf(audioManager).getDevices(AudioManager.GET_DEVICES_ALL)))
        .containsExactly(scoDevice, a2dpDevice);
  }

  @Test
  @Config(minSdk = S)
  public void setCommunicationDevice_updatesCommunicationDevice() throws Exception {
    AudioDeviceInfo scoDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    audioManager.setCommunicationDevice(scoDevice);

    assertThat(audioManager.getCommunicationDevice()).isEqualTo(scoDevice);
  }

  @Test
  @Config(minSdk = S)
  public void lockCommunicationDevice_locked_deviceIsNotSet() throws Exception {
    AudioDeviceInfo scoDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).lockCommunicationDevice(true);

    audioManager.setCommunicationDevice(scoDevice);

    assertThat(audioManager.getCommunicationDevice()).isNull();
  }

  @Test
  @Config(minSdk = S)
  public void lockCommunicationDevice_notLocked_deviceIsSet() throws Exception {
    AudioDeviceInfo scoDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    shadowOf(audioManager).lockCommunicationDevice(false);

    audioManager.setCommunicationDevice(scoDevice);

    assertThat(audioManager.getCommunicationDevice()).isEqualTo(scoDevice);
  }

  @Test
  @Config(minSdk = S)
  public void clearCommunicationDevice_clearsCommunicationDevice() throws Exception {
    AudioDeviceInfo scoDevice = createAudioDevice(DEVICE_OUT_BLUETOOTH_SCO);
    audioManager.setCommunicationDevice(scoDevice);
    assertThat(audioManager.getCommunicationDevice()).isEqualTo(scoDevice);

    audioManager.clearCommunicationDevice();
    assertThat(audioManager.getCommunicationDevice()).isNull();
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
        .setActivePlaybackConfigurationsFor(Arrays.asList(movieAttribute, musicAttribute));
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

    verifyNoMoreInteractions(callback);
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
  public void adjustStreamVolume_lower() {
    shadowOf(audioManager).setStreamVolume(7);
    int volumeBefore = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, /* flags= */ 0);

    int volumeAfter = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    assertThat(volumeAfter).isLessThan(volumeBefore);
  }

  @Test
  @Config(minSdk = M)
  public void adjustStreamVolume_lowerAtMinVolume_remainsSame() {
    shadowOf(audioManager).setStreamVolume(1);
    int volumeBefore = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, /* flags= */ 0);

    int volumeAfter = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    assertThat(volumeAfter).isEqualTo(volumeBefore);
  }

  @Test
  @Config(minSdk = M)
  public void adjustStreamVolume_raise() {
    shadowOf(audioManager).setStreamVolume(7);
    int volumeBefore = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, /* flags= */ 0);

    int volumeAfter = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    assertThat(volumeAfter).isGreaterThan(volumeBefore);
  }

  @Test
  @Config(minSdk = M)
  public void adjustStreamVolume_raiseAtMaxVolume_remainsSame() {
    shadowOf(audioManager).setStreamVolume(7);
    shadowOf(audioManager).setStreamMaxVolume(7);
    int volumeBefore = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, /* flags= */ 0);

    int volumeAfter = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    assertThat(volumeAfter).isEqualTo(volumeBefore);
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

    verifyNoMoreInteractions(callback);
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
  public void generateAudioSessionId_returnsPositiveValues() {
    int audioSessionId = audioManager.generateAudioSessionId();
    int audioSessionId2 = audioManager.generateAudioSessionId();

    assertThat(audioSessionId).isGreaterThan(0);
    assertThat(audioSessionId2).isGreaterThan(0);
  }

  @Test
  public void generateAudioSessionId_returnsDistinctValues() {
    int audioSessionId = audioManager.generateAudioSessionId();
    int audioSessionId2 = audioManager.generateAudioSessionId();

    assertThat(audioSessionId).isNotEqualTo(audioSessionId2);
  }

  @Test
  @Config(minSdk = Q)
  public void isOffloadSupported_withoutSupport() {
    assertThat(
            AudioManager.isOffloadedPlaybackSupported(
                new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_AC3).build(),
                new AudioAttributes.Builder().build()))
        .isFalse();
  }

  @Test
  @Config(minSdk = Q, maxSdk = R)
  public void isOffloadSupported_withSetOffloadSupported() {
    AudioFormat format =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    assertThat(AudioManager.isOffloadedPlaybackSupported(format, attributes)).isFalse();

    ShadowAudioSystem.setOffloadSupported(format, attributes, true);

    assertThat(AudioManager.isOffloadedPlaybackSupported(format, attributes)).isTrue();
  }

  @Test
  @Config(minSdk = Q, maxSdk = R)
  public void isOffloadSupported_withSetOffloadSupportedAddedAndRemoved() {
    AudioFormat format =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    ShadowAudioSystem.setOffloadSupported(format, attributes, true);
    assertThat(AudioManager.isOffloadedPlaybackSupported(format, attributes)).isTrue();

    ShadowAudioSystem.setOffloadSupported(format, attributes, false);

    assertThat(AudioManager.isOffloadedPlaybackSupported(format, attributes)).isFalse();
  }

  @Test
  @Config(minSdk = S)
  public void isOffloadSupported_withSetOffloadPlaybackSupport() {
    AudioFormat format =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    assertThat(AudioManager.isOffloadedPlaybackSupported(format, attributes)).isFalse();

    ShadowAudioSystem.setOffloadPlaybackSupport(format, attributes, AudioSystem.OFFLOAD_SUPPORTED);

    assertThat(AudioManager.isOffloadedPlaybackSupported(format, attributes)).isTrue();
  }

  @Test
  @Config(minSdk = S)
  public void getPlaybackOffloadSupport_withSetOffloadSupport_returnsOffloadSupported() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setSampleRate(48_000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_AAC_HE_V2)
            .build();
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();
    ShadowAudioSystem.setOffloadPlaybackSupport(
        audioFormat, audioAttributes, AudioSystem.OFFLOAD_SUPPORTED);

    int playbackOffloadSupport =
        AudioManager.getPlaybackOffloadSupport(audioFormat, audioAttributes);

    assertThat(playbackOffloadSupport).isEqualTo(AudioSystem.OFFLOAD_SUPPORTED);
  }

  @Test
  @Config(minSdk = S)
  public void
      getPlaybackOffloadSupport_withoutSetDirectPlaybackSupport_returnsOffloadNotSupported() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setSampleRate(48_000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_AAC_HE_V2)
            .build();
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build();

    int playbackOffloadSupport =
        AudioManager.getPlaybackOffloadSupport(audioFormat, audioAttributes);

    assertThat(playbackOffloadSupport).isEqualTo(AudioSystem.OFFLOAD_NOT_SUPPORTED);
  }

  @Test
  @Config(minSdk = S)
  public void getPlaybackOffloadSupport_withSameAudioAttrUsage_returnsOffloadSupported() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setSampleRate(48_000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_AAC_HE_V2)
            .build();
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();
    ShadowAudioSystem.setOffloadPlaybackSupport(
        audioFormat, audioAttributes, AudioSystem.OFFLOAD_SUPPORTED);

    AudioAttributes audioAttributes2 =
        new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();
    int playbackOffloadSupport =
        AudioManager.getPlaybackOffloadSupport(audioFormat, audioAttributes2);

    assertThat(playbackOffloadSupport).isEqualTo(AudioSystem.OFFLOAD_SUPPORTED);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getDirectPlaybackSupport_withSetDirectPlaybackSupport_returnsOffloadSupported() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setSampleRate(48_000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_AAC_HE_V2)
            .build();
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();
    ShadowAudioSystem.setDirectPlaybackSupport(
        audioFormat, audioAttributes, AudioSystem.DIRECT_OFFLOAD_SUPPORTED);

    int playbackOffloadSupport =
        AudioManager.getDirectPlaybackSupport(audioFormat, audioAttributes);

    assertThat(playbackOffloadSupport).isEqualTo(AudioSystem.DIRECT_OFFLOAD_SUPPORTED);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getDirectPlaybackSupport_withShadowAudioSystemReset_returnsOffloadNotSupported() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setSampleRate(48_000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_AAC_HE_V2)
            .build();
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build();
    ShadowAudioSystem.setDirectPlaybackSupport(
        audioFormat, audioAttributes, AudioSystem.DIRECT_OFFLOAD_SUPPORTED);
    ShadowAudioSystem.reset();

    int playbackOffloadSupport =
        AudioManager.getDirectPlaybackSupport(audioFormat, audioAttributes);

    assertThat(playbackOffloadSupport).isEqualTo(AudioSystem.DIRECT_NOT_SUPPORTED);
  }

  @Test
  public void dispatchMediaKeyEvent_recordsEvent() {
    KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);

    audioManager.dispatchMediaKeyEvent(keyEvent);

    assertThat(shadowOf(audioManager).getDispatchedMediaKeyEvents()).containsExactly(keyEvent);
  }

  @Test
  public void clearDispatchedMediaKeyEvents_clearsDispatchedEvents() {
    audioManager.dispatchMediaKeyEvent(
        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));

    shadowOf(audioManager).clearDispatchedMediaKeyEvents();

    assertThat(shadowOf(audioManager).getDispatchedMediaKeyEvents()).isEmpty();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void setHotwordStreamSupportedWithLookbackAudio_updatesIsHotwordStreamSupported() {
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ true))
        .isFalse();
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ false))
        .isFalse();

    shadowOf(audioManager)
        .setHotwordStreamSupported(/* lookbackAudio= */ true, /* isSupported= */ true);

    // isHotwordStreamSupported with lookbackAudio=true is set.
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ true)).isTrue();
    // isHotwordStreamSupported with lookbackAudio=false is not set.
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ false))
        .isFalse();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void setHotwordStreamSupportedWithoutLookbackAudio_updatesIsHotwordStreamSupported() {
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ false))
        .isFalse();
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ true))
        .isFalse();

    shadowOf(audioManager)
        .setHotwordStreamSupported(/* lookbackAudio= */ false, /* isSupported= */ true);

    // isHotwordStreamSupported with lookbackAudio=false is set.
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ false))
        .isTrue();
    // isHotwordStreamSupported with lookbackAudio=true is not set.
    assertThat(shadowOf(audioManager).isHotwordStreamSupported(/* lookbackAudio= */ true))
        .isFalse();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getDirectProfilesForAttributes_returnsEmptyListByDefault() {
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
            .build();

    assertThat(shadowOf(audioManager).getDirectProfilesForAttributes(audioAttributes)).isEmpty();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void
      addAndRemoveOutputDeviceWithDirectProfiles_updatesDirectProfilesForAttributes_notifiesCallback() {
    AudioDeviceCallback callback = mock(AudioDeviceCallback.class);
    audioManager.registerAudioDeviceCallback(callback, /* handler= */ null);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {}); // initial registration
    ImmutableList<AudioProfile> expectedProfiles =
        ImmutableList.of(
            AudioProfileBuilder.newBuilder()
                .setFormat(AudioFormat.ENCODING_AC3)
                .setSamplingRates(new int[] {48_000})
                .setChannelMasks(new int[] {AudioFormat.CHANNEL_OUT_5POINT1})
                .setEncapsulationType(AudioProfile.AUDIO_ENCAPSULATION_TYPE_NONE)
                .build());
    AudioDeviceInfo outputDevice =
        AudioDeviceInfoBuilder.newBuilder()
            .setType(AudioDeviceInfo.TYPE_HDMI)
            .setProfiles(expectedProfiles)
            .build();
    AudioAttributes audioAttributes =
        new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
            .build();

    shadowOf(audioManager).addOutputDeviceWithDirectProfiles(outputDevice);

    assertThat(shadowOf(audioManager).getDirectProfilesForAttributes(audioAttributes))
        .isEqualTo(expectedProfiles);
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {outputDevice});

    shadowOf(audioManager).removeOutputDeviceWithDirectProfiles(outputDevice);

    assertThat(shadowOf(audioManager).getDirectProfilesForAttributes(audioAttributes)).isEmpty();
    verify(callback).onAudioDevicesAdded(new AudioDeviceInfo[] {outputDevice});
  }

  @Test
  @Config(minSdk = O)
  public void audioManager_activityContextEnabled_applicationInstanceIsNotSameAsActivityInstance() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try {
      AudioManager applicationAudioManager = appContext.getSystemService(AudioManager.class);
      Activity activity = Robolectric.setupActivity(Activity.class);
      AudioManager activityAudioManager = activity.getSystemService(AudioManager.class);
      assertThat(applicationAudioManager).isNotSameInstanceAs(activityAudioManager);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }

  @Test
  @Config(minSdk = O)
  public void audioManager_activityContextEnabled_activityInstanceIsSameAsActivityInstance() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try {
      Activity activity = Robolectric.setupActivity(Activity.class);
      AudioManager activityAudioManager = activity.getSystemService(AudioManager.class);
      AudioManager anotherActivityAudioManager = activity.getSystemService(AudioManager.class);
      assertThat(anotherActivityAudioManager).isSameInstanceAs(activityAudioManager);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }

  @Test
  @Config(minSdk = O)
  public void audioManager_activityContextEnabled_differentInstancesChangesAffectEachOther() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try {
      AudioManager applicationAudioManager = appContext.getSystemService(AudioManager.class);
      Activity activity = Robolectric.setupActivity(Activity.class);
      AudioManager activityAudioManager = activity.getSystemService(AudioManager.class);

      activityAudioManager.setMode(AudioManager.MODE_RINGTONE);
      assertThat(activityAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);
      assertThat(applicationAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);

      applicationAudioManager.setMode(AudioManager.MODE_NORMAL);
      assertThat(activityAudioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
      assertThat(applicationAudioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }

  private static AudioDeviceInfo createAudioDevice(int type) throws ReflectiveOperationException {
    AudioDeviceInfo info = Shadow.newInstanceOf(AudioDeviceInfo.class);
    Field portField = AudioDeviceInfo.class.getDeclaredField("mPort");
    portField.setAccessible(true);
    Object port = Shadow.newInstanceOf("android.media.AudioDevicePort");
    portField.set(info, port);

    Field typeField = port.getClass().getDeclaredField("mType");
    typeField.setAccessible(true);
    typeField.set(port, type);

    return info;
  }

  private static class LocalOnModeChangedListener implements OnModeChangedListener {
    private List<Integer> modes = new ArrayList<>();

    @Override
    public void onModeChanged(int mode) {
      modes.add(mode);
    }
  }
}

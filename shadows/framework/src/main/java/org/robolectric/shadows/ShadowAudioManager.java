package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AudioManager.class)
public class ShadowAudioManager {
  public static final int MAX_VOLUME_MUSIC_DTMF = 15;
  public static final int DEFAULT_MAX_VOLUME = 7;
  public static final int DEFAULT_VOLUME = 7;
  public static final int INVALID_VOLUME = 0;
  public static final int FLAG_NO_ACTION = 0;
  public static final int[] ALL_STREAMS = {
      AudioManager.STREAM_MUSIC,
      AudioManager.STREAM_ALARM,
      AudioManager.STREAM_NOTIFICATION,
      AudioManager.STREAM_RING,
      AudioManager.STREAM_SYSTEM,
      AudioManager.STREAM_VOICE_CALL,
      AudioManager.STREAM_DTMF
  };

  private AudioFocusRequest lastAudioFocusRequest;
  private int nextResponseValue = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  private AudioManager.OnAudioFocusChangeListener lastAbandonedAudioFocusListener;
  private android.media.AudioFocusRequest lastAbandonedAudioFocusRequest;
  private HashMap<Integer, AudioStream> streamStatus = new HashMap<>();
  private List<AudioPlaybackConfiguration> activePlaybackConfigurations = Collections.emptyList();
  private int ringerMode = AudioManager.RINGER_MODE_NORMAL;
  private int mode = AudioManager.MODE_NORMAL;
  private boolean bluetoothA2dpOn;
  private boolean isBluetoothScoOn;
  private boolean isSpeakerphoneOn;
  private boolean isMicrophoneMuted = false;
  private boolean isMusicActive;
  private boolean wiredHeadsetOn;

  public ShadowAudioManager() {
    for (int stream : ALL_STREAMS) {
      streamStatus.put(stream, new AudioStream(DEFAULT_VOLUME, DEFAULT_MAX_VOLUME, FLAG_NO_ACTION));
    }
    streamStatus.get(AudioManager.STREAM_MUSIC).setMaxVolume(MAX_VOLUME_MUSIC_DTMF);
    streamStatus.get(AudioManager.STREAM_DTMF).setMaxVolume(MAX_VOLUME_MUSIC_DTMF);
  }

  @Implementation
  protected int getStreamMaxVolume(int streamType) {
    AudioStream stream = streamStatus.get(streamType);
    return (stream != null) ? stream.getMaxVolume() : INVALID_VOLUME;
  }

  @Implementation
  protected int getStreamVolume(int streamType) {
    AudioStream stream = streamStatus.get(streamType);
    return (stream != null) ? stream.getCurrentVolume() : INVALID_VOLUME;
  }

  @Implementation
  protected void setStreamVolume(int streamType, int index, int flags) {
    AudioStream stream = streamStatus.get(streamType);
    if (stream != null) {
      stream.setCurrentVolume(index);
      stream.setFlag(flags);
    }
  }

  @Implementation
  protected int requestAudioFocus(
      android.media.AudioManager.OnAudioFocusChangeListener l, int streamType, int durationHint) {
    lastAudioFocusRequest = new AudioFocusRequest(l, streamType, durationHint);
    return nextResponseValue;
  }

  /**
   * Provides a mock like interface for the requestAudioFocus method by storing the request
   * object for later inspection and returning the value specified in setNextFocusRequestResponse.
   */
  @Implementation(minSdk = O)
  protected int requestAudioFocus(android.media.AudioFocusRequest audioFocusRequest) {
    lastAudioFocusRequest = new AudioFocusRequest(audioFocusRequest);
    return nextResponseValue;
  }

  @Implementation
  protected int abandonAudioFocus(AudioManager.OnAudioFocusChangeListener l) {
    lastAbandonedAudioFocusListener = l;
    return nextResponseValue;
  }


  /**
   * Provides a mock like interface for the abandonAudioFocusRequest method by storing the request
   * object for later inspection and returning the value specified in setNextFocusRequestResponse.
   */
  @Implementation(minSdk = O)
  protected int abandonAudioFocusRequest(android.media.AudioFocusRequest audioFocusRequest) {
    lastAbandonedAudioFocusRequest = audioFocusRequest;
    return nextResponseValue;
  }

  @Implementation
  protected int getRingerMode() {
    return ringerMode;
  }

  @Implementation
  protected void setRingerMode(int ringerMode) {
    if (!AudioManager.isValidRingerMode(ringerMode)) {
      return;
    }
    this.ringerMode = ringerMode;
  }

  public static boolean isValidRingerMode(int ringerMode) {
    if (ringerMode < 0 || ringerMode > (int)ReflectionHelpers.getStaticField(AudioManager.class, "RINGER_MODE_MAX")) {
      return false;
    }
    return true;
  }

  @Implementation
  protected void setMode(int mode) {
    this.mode = mode;
  }

  @Implementation
  protected int getMode() {
    return this.mode;
  }

  public void setStreamMaxVolume(int streamMaxVolume) {
    for (Map.Entry<Integer, AudioStream> entry : streamStatus.entrySet()) {
      entry.getValue().setMaxVolume(streamMaxVolume);
    }
  }

  public void setStreamVolume(int streamVolume) {
    for (Map.Entry<Integer, AudioStream> entry : streamStatus.entrySet()) {
      entry.getValue().setCurrentVolume(streamVolume);
    }
  }

  @Implementation
  protected void setWiredHeadsetOn(boolean on) {
    wiredHeadsetOn = on;
  }

  @Implementation
  protected boolean isWiredHeadsetOn() {
    return wiredHeadsetOn;
  }

  @Implementation
  protected void setBluetoothA2dpOn(boolean on) {
    bluetoothA2dpOn = on;
  }

  @Implementation
  protected boolean isBluetoothA2dpOn() {
    return bluetoothA2dpOn;
  }

  @Implementation
  protected void setSpeakerphoneOn(boolean on) {
    isSpeakerphoneOn = on;
  }

  @Implementation
  protected boolean isSpeakerphoneOn() {
    return isSpeakerphoneOn;
  }

  @Implementation
  protected void setMicrophoneMute(boolean on) {
    isMicrophoneMuted = on;
  }

  @Implementation
  protected boolean isMicrophoneMute() {
    return isMicrophoneMuted;
  }

  @Implementation
  protected boolean isBluetoothScoOn() {
    return isBluetoothScoOn;
  }

  @Implementation
  protected void setBluetoothScoOn(boolean isBluetoothScoOn) {
    this.isBluetoothScoOn = isBluetoothScoOn;
  }

  @Implementation
  protected boolean isMusicActive() {
    return isMusicActive;
  }

  @Implementation(minSdk = O)
  protected List<AudioPlaybackConfiguration> getActivePlaybackConfigurations() {
    return new ArrayList<>(activePlaybackConfigurations);
  }

  /**
   * Sets active playback configurations that will be served by {@link
   * AudioManager#getActivePlaybackConfigurations}.
   *
   * <p>Note that there is no public {@link AudioPlaybackConfiguration} constructor, so the
   * configurations returned are specified by their audio attributes only.
   */
  @TargetApi(VERSION_CODES.O)
  public void setActivePlaybackConfigurationsFor(List<AudioAttributes> audioAttributes) {
    activePlaybackConfigurations = new ArrayList<>(audioAttributes.size());
    for (AudioAttributes audioAttribute : audioAttributes) {
      Parcel p = Parcel.obtain();
      p.writeInt(0); // mPlayerIId
      p.writeInt(0); // mPlayerType
      p.writeInt(0); // mClientUid
      p.writeInt(0); // mClientPid
      p.writeInt(AudioPlaybackConfiguration.PLAYER_STATE_STARTED); // mPlayerState
      audioAttribute.writeToParcel(p, 0);
      p.writeStrongInterface(null);
      byte[] bytes = p.marshall();
      p.recycle();
      p = Parcel.obtain();
      p.unmarshall(bytes, 0, bytes.length);
      AudioPlaybackConfiguration configuration =
          AudioPlaybackConfiguration.CREATOR.createFromParcel(p);
      p.recycle();
      activePlaybackConfigurations.add(configuration);
    }
  }

  public void setIsMusicActive(boolean isMusicActive) {
    this.isMusicActive = isMusicActive;
  }

  public AudioFocusRequest getLastAudioFocusRequest() {
    return lastAudioFocusRequest;
  }

  public void setNextFocusRequestResponse(int nextResponseValue) {
    this.nextResponseValue = nextResponseValue;
  }

  public AudioManager.OnAudioFocusChangeListener getLastAbandonedAudioFocusListener() {
    return lastAbandonedAudioFocusListener;
  }

  public android.media.AudioFocusRequest getLastAbandonedAudioFocusRequest() {
    return lastAbandonedAudioFocusRequest;
  }

  public static class AudioFocusRequest {
    public final AudioManager.OnAudioFocusChangeListener listener;
    public final int streamType;
    public final int durationHint;
    public final android.media.AudioFocusRequest audioFocusRequest;

    private AudioFocusRequest(AudioManager.OnAudioFocusChangeListener listener, int streamType, int durationHint) {
      this.listener = listener;
      this.streamType = streamType;
      this.durationHint = durationHint;
      this.audioFocusRequest = null;
    }

    private AudioFocusRequest(android.media.AudioFocusRequest audioFocusRequest) {
      this.listener = null;
      this.streamType = this.durationHint = -1;
      this.audioFocusRequest = audioFocusRequest;
    }
  }

  private static class AudioStream {
    private int currentVolume;
    private int maxVolume;
    private int flag;

    public AudioStream(int currVol, int maxVol, int flag) {
      setCurrentVolume(currVol);
      setMaxVolume(maxVol);
      setFlag(flag);
    }

    public int getCurrentVolume() {
      return currentVolume;
    }

    public int getMaxVolume() {
      return maxVolume;
    }

    public int getFlag() {
      return flag;
    }

    public void setCurrentVolume(int vol) {
      if (vol > maxVolume) {
        vol = maxVolume;
      } else if (vol < 0) {
        vol = 0;
      }
      currentVolume = vol;
    }

    public void setMaxVolume(int vol) {
      maxVolume = vol;
    }

    public void setFlag(int flag) {
      this.flag = flag;
    }
  }
}

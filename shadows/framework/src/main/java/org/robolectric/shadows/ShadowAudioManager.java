package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.annotation.NonNull;
import android.annotation.RequiresPermission;
import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRecordingConfiguration;
import android.media.IPlayer;
import android.media.PlayerBase;
import android.media.audiopolicy.AudioPolicy;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Parcel;
import com.android.internal.util.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = AudioManager.class, looseSignatures = true)
public class ShadowAudioManager {

  public static final int MAX_VOLUME_MUSIC_DTMF = 15;
  public static final int DEFAULT_MAX_VOLUME = 7;
  public static final int MIN_VOLUME = 0;
  public static final int DEFAULT_VOLUME = 7;
  public static final int INVALID_VOLUME = 0;
  public static final int FLAG_NO_ACTION = 0;
  public static final ImmutableList<Integer> ALL_STREAMS =
      ImmutableList.of(
          AudioManager.STREAM_MUSIC,
          AudioManager.STREAM_ALARM,
          AudioManager.STREAM_NOTIFICATION,
          AudioManager.STREAM_RING,
          AudioManager.STREAM_SYSTEM,
          AudioManager.STREAM_VOICE_CALL,
          AudioManager.STREAM_DTMF);

  private static final int INVALID_PATCH_HANDLE = -1;
  private static final float MAX_VOLUME_DB = 0;
  private static final float MIN_VOLUME_DB = -100;

  private AudioFocusRequest lastAudioFocusRequest;
  private int nextResponseValue = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  private AudioManager.OnAudioFocusChangeListener lastAbandonedAudioFocusListener;
  private android.media.AudioFocusRequest lastAbandonedAudioFocusRequest;
  private HashMap<Integer, AudioStream> streamStatus = new HashMap<>();
  private List<AudioPlaybackConfiguration> activePlaybackConfigurations = Collections.emptyList();
  private List<AudioRecordingConfiguration> activeRecordingConfigurations = ImmutableList.of();
  private final HashSet<AudioManager.AudioRecordingCallback> audioRecordingCallbacks =
      new HashSet<>();
  private final HashSet<AudioManager.AudioPlaybackCallback> audioPlaybackCallbacks =
      new HashSet<>();
  private int ringerMode = AudioManager.RINGER_MODE_NORMAL;
  private int mode = AudioManager.MODE_NORMAL;
  private boolean bluetoothA2dpOn;
  private boolean isBluetoothScoOn;
  private boolean isSpeakerphoneOn;
  private boolean isMicrophoneMuted = false;
  private boolean isMusicActive;
  private boolean wiredHeadsetOn;
  private boolean isBluetoothScoAvailableOffCall = false;
  private final Map<String, String> parameters = new HashMap<>();
  private final Map<Integer, Boolean> streamsMuteState = new HashMap<>();
  private final Map<String, AudioPolicy> registeredAudioPolicies = new HashMap<>();
  private int audioSessionIdCounter = 1;
  private final Map<AudioAttributes, ImmutableList<Object>> devicesForAttributes = new HashMap<>();
  private ImmutableList<Object> defaultDevicesForAttributes = ImmutableList.of();

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

  @Implementation(minSdk = P)
  protected float getStreamVolumeDb(int streamType, int index, int deviceType) {
    AudioStream stream = streamStatus.get(streamType);
    if (stream == null) {
      return INVALID_VOLUME;
    }
    if (index < MIN_VOLUME || index > stream.getMaxVolume()) {
      throw new IllegalArgumentException("Invalid stream volume index " + index);
    }
    if (index == MIN_VOLUME) {
      return Float.NEGATIVE_INFINITY;
    }
    float interpolation = (index - MIN_VOLUME) / (float) (stream.getMaxVolume() - MIN_VOLUME);
    return MIN_VOLUME_DB + interpolation * (MAX_VOLUME_DB - MIN_VOLUME_DB);
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
  protected boolean isBluetoothScoAvailableOffCall() {
    return isBluetoothScoAvailableOffCall;
  }

  @Implementation
  protected int requestAudioFocus(
      android.media.AudioManager.OnAudioFocusChangeListener l, int streamType, int durationHint) {
    lastAudioFocusRequest = new AudioFocusRequest(l, streamType, durationHint);
    return nextResponseValue;
  }

  /**
   * Provides a mock like interface for the requestAudioFocus method by storing the request object
   * for later inspection and returning the value specified in setNextFocusRequestResponse.
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
    lastAbandonedAudioFocusListener = audioFocusRequest.getOnAudioFocusChangeListener();
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
    return ringerMode >= 0
        && ringerMode
            <= (int) ReflectionHelpers.getStaticField(AudioManager.class, "RINGER_MODE_MAX");
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
    streamStatus.forEach((key, value) -> value.setMaxVolume(streamMaxVolume));
  }

  public void setStreamVolume(int streamVolume) {
    streamStatus.forEach((key, value) -> value.setCurrentVolume(streamVolume));
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

  @Implementation
  protected void setParameters(String keyValuePairs) {
    if (keyValuePairs.isEmpty()) {
      throw new IllegalArgumentException("keyValuePairs should not be empty");
    }

    if (keyValuePairs.charAt(keyValuePairs.length() - 1) != ';') {
      throw new IllegalArgumentException("keyValuePairs should end with a ';'");
    }

    String[] pairs = keyValuePairs.split(";", 0);

    for (String pair : pairs) {
      if (pair.isEmpty()) {
        continue;
      }

      String[] splittedPair = pair.split("=", 0);
      if (splittedPair.length != 2) {
        throw new IllegalArgumentException(
            "keyValuePairs: each pair should be in the format of key=value;");
      }
      parameters.put(splittedPair[0], splittedPair[1]);
    }
  }

  /**
   * The expected composition for keys is not well defined.
   *
   * <p>For testing purposes this method call always returns null.
   */
  @Implementation
  protected String getParameters(String keys) {
    return null;
  }

  /** Returns a single parameter that was set via {@link #setParameters(String)}. */
  public String getParameter(String key) {
    return parameters.get(key);
  }

  /**
   * Implements {@link AudioManager#adjustStreamVolume(int, int, int)}.
   *
   * <p>Currently supports only the directions {@link AudioManager#ADJUST_MUTE}, {@link
   * AudioManager#ADJUST_UNMUTE}, {@link AudioManager#ADJUST_LOWER} and {@link
   * AudioManager#ADJUST_RAISE}.
   */
  @Implementation
  protected void adjustStreamVolume(int streamType, int direction, int flags) {
    int streamVolume = getStreamVolume(streamType);
    switch (direction) {
      case AudioManager.ADJUST_MUTE:
        streamsMuteState.put(streamType, true);
        break;
      case AudioManager.ADJUST_UNMUTE:
        streamsMuteState.put(streamType, false);
        break;
      case AudioManager.ADJUST_RAISE:
        int streamMaxVolume = getStreamMaxVolume(streamType);
        if (streamVolume == INVALID_VOLUME || streamMaxVolume == INVALID_VOLUME) {
          return;
        }
        int raisedVolume = streamVolume + 1;
        if (raisedVolume <= streamMaxVolume) {
          setStreamVolume(raisedVolume);
        }
        break;
      case AudioManager.ADJUST_LOWER:
        if (streamVolume == INVALID_VOLUME) {
          return;
        }
        int lowerVolume = streamVolume - 1;
        if (lowerVolume >= 1) {
          setStreamVolume(lowerVolume);
        }
        break;
      default:
        break;
    }
  }

  @Implementation(minSdk = M)
  protected boolean isStreamMute(int streamType) {
    if (!streamsMuteState.containsKey(streamType)) {
      return false;
    }
    return streamsMuteState.get(streamType);
  }

  public void setIsBluetoothScoAvailableOffCall(boolean isBluetoothScoAvailableOffCall) {
    this.isBluetoothScoAvailableOffCall = isBluetoothScoAvailableOffCall;
  }

  public void setIsStreamMute(int streamType, boolean isMuted) {
    streamsMuteState.put(streamType, isMuted);
  }

  /**
   * Registers callback that will receive changes made to the list of active playback configurations
   * by {@link setActivePlaybackConfigurationsFor}.
   */
  @Implementation(minSdk = O)
  protected void registerAudioPlaybackCallback(
      AudioManager.AudioPlaybackCallback cb, Handler handler) {
    audioPlaybackCallbacks.add(cb);
  }

  /** Unregisters callback listening to changes made to list of active playback configurations. */
  @Implementation(minSdk = O)
  protected void unregisterAudioPlaybackCallback(AudioManager.AudioPlaybackCallback cb) {
    audioPlaybackCallbacks.remove(cb);
  }

  /**
   * Returns the devices associated with the given audio stream.
   *
   * <p>In this shadow-implementation the devices returned are either
   *
   * <ol>
   *   <li>devices set through {@link #setDevicesForAttributes}, or
   *   <li>devices set through {@link #setDefaultDevicesForAttributes}, or
   *   <li>an empty list.
   * </ol>
   */
  @Implementation(minSdk = R)
  @NonNull
  protected List<Object> getDevicesForAttributes(@NonNull AudioAttributes attributes) {
    ImmutableList<Object> devices = devicesForAttributes.get(attributes);
    return devices == null ? defaultDevicesForAttributes : devices;
  }

  /** Sets the devices associated with the given audio stream. */
  public void setDevicesForAttributes(
      @NonNull AudioAttributes attributes, @NonNull ImmutableList<Object> devices) {
    devicesForAttributes.put(attributes, devices);
  }

  /** Sets the devices to use as default for all audio streams. */
  public void setDefaultDevicesForAttributes(@NonNull ImmutableList<Object> devices) {
    defaultDevicesForAttributes = devices;
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
    setActivePlaybackConfigurationsFor(audioAttributes, /* notifyCallbackListeners= */ false);
  }

  /**
   * Same as {@link #setActivePlaybackConfigurationsFor(List)}, but also notifies callbacks if
   * notifyCallbackListeners is true.
   */
  @TargetApi(VERSION_CODES.O)
  public void setActivePlaybackConfigurationsFor(
      List<AudioAttributes> audioAttributes, boolean notifyCallbackListeners) {
    if (RuntimeEnvironment.getApiLevel() < O) {
      throw new UnsupportedOperationException(
          "setActivePlaybackConfigurationsFor is not supported on API "
              + RuntimeEnvironment.getApiLevel());
    }
    activePlaybackConfigurations = new ArrayList<>(audioAttributes.size());
    for (AudioAttributes audioAttribute : audioAttributes) {
      AudioPlaybackConfiguration configuration = createAudioPlaybackConfiguration(audioAttribute);
      activePlaybackConfigurations.add(configuration);
    }
    if (notifyCallbackListeners) {
      for (AudioManager.AudioPlaybackCallback callback : audioPlaybackCallbacks) {
        callback.onPlaybackConfigChanged(activePlaybackConfigurations);
      }
    }
  }

  protected AudioPlaybackConfiguration createAudioPlaybackConfiguration(
      AudioAttributes audioAttributes) {
    // use reflection to call package private APIs
    if (RuntimeEnvironment.getApiLevel() >= S) {
      PlayerBase.PlayerIdCard playerIdCard =
          ReflectionHelpers.callConstructor(
              PlayerBase.PlayerIdCard.class,
              ReflectionHelpers.ClassParameter.from(int.class, 0), /* type */
              ReflectionHelpers.ClassParameter.from(AudioAttributes.class, audioAttributes),
              ReflectionHelpers.ClassParameter.from(IPlayer.class, null),
              ReflectionHelpers.ClassParameter.from(int.class, 0) /* sessionId */);
      AudioPlaybackConfiguration config =
          ReflectionHelpers.callConstructor(
              AudioPlaybackConfiguration.class,
              ReflectionHelpers.ClassParameter.from(PlayerBase.PlayerIdCard.class, playerIdCard),
              ReflectionHelpers.ClassParameter.from(int.class, 0), /* piid */
              ReflectionHelpers.ClassParameter.from(int.class, 0), /* uid */
              ReflectionHelpers.ClassParameter.from(int.class, 0) /* pid */);
      ReflectionHelpers.setField(
          config, "mPlayerState", AudioPlaybackConfiguration.PLAYER_STATE_STARTED);
      return config;
    } else {
      PlayerBase.PlayerIdCard playerIdCard =
          ReflectionHelpers.callConstructor(
              PlayerBase.PlayerIdCard.class,
              from(int.class, 0), /* type */
              from(AudioAttributes.class, audioAttributes),
              from(IPlayer.class, null));
      AudioPlaybackConfiguration config =
          ReflectionHelpers.callConstructor(
              AudioPlaybackConfiguration.class,
              from(PlayerBase.PlayerIdCard.class, playerIdCard),
              from(int.class, 0), /* piid */
              from(int.class, 0), /* uid */
              from(int.class, 0) /* pid */);
      ReflectionHelpers.setField(
          config, "mPlayerState", AudioPlaybackConfiguration.PLAYER_STATE_STARTED);
      return config;
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

  /**
   * Returns list of active recording configurations that was set by {@link
   * #setActiveRecordingConfigurations} or empty list otherwise.
   */
  @Implementation(minSdk = N)
  protected List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
    return activeRecordingConfigurations;
  }

  /**
   * Registers callback that will receive changes made to the list of active recording
   * configurations by {@link setActiveRecordingConfigurations}.
   */
  @Implementation(minSdk = N)
  protected void registerAudioRecordingCallback(
      AudioManager.AudioRecordingCallback cb, Handler handler) {
    audioRecordingCallbacks.add(cb);
  }

  /** Unregisters callback listening to changes made to list of active recording configurations. */
  @Implementation(minSdk = N)
  protected void unregisterAudioRecordingCallback(AudioManager.AudioRecordingCallback cb) {
    audioRecordingCallbacks.remove(cb);
  }

  /**
   * Sets active recording configurations that will be served by {@link
   * AudioManager#getActiveRecordingConfigurations} and notifies callback listeners about that
   * change.
   */
  public void setActiveRecordingConfigurations(
      List<AudioRecordingConfiguration> activeRecordingConfigurations,
      boolean notifyCallbackListeners) {
    this.activeRecordingConfigurations = new ArrayList<>(activeRecordingConfigurations);

    if (notifyCallbackListeners) {
      for (AudioManager.AudioRecordingCallback callback : audioRecordingCallbacks) {
        callback.onRecordingConfigChanged(this.activeRecordingConfigurations);
      }
    }
  }

  /**
   * Creates simple active recording configuration. The resulting configuration will return {@code
   * null} for {@link android.media.AudioRecordingConfiguration#getAudioDevice}.
   */
  public AudioRecordingConfiguration createActiveRecordingConfiguration(
      int sessionId, int audioSource, String clientPackageName) {
    Parcel p = Parcel.obtain();
    p.writeInt(sessionId); // mSessionId
    p.writeInt(audioSource); // mClientSource
    writeMono16BitAudioFormatToParcel(p); // mClientFormat
    writeMono16BitAudioFormatToParcel(p); // mDeviceFormat
    p.writeInt(INVALID_PATCH_HANDLE); // mPatchHandle
    p.writeString(clientPackageName); // mClientPackageName
    p.writeInt(0); // mClientUid

    p.setDataPosition(0);

    AudioRecordingConfiguration configuration =
        AudioRecordingConfiguration.CREATOR.createFromParcel(p);
    p.recycle();

    return configuration;
  }

  /**
   * Registers an {@link AudioPolicy} to allow that policy to control audio routing and audio focus.
   *
   * <p>Note: this implementation does NOT ensure that we have the permissions necessary to register
   * the given {@link AudioPolicy}.
   *
   * @return {@link AudioManager.ERROR} if the given policy has already been registered, and {@link
   *     AudioManager.SUCCESS} otherwise.
   */
  @HiddenApi
  @Implementation(minSdk = P)
  @RequiresPermission(android.Manifest.permission.MODIFY_AUDIO_ROUTING)
  protected int registerAudioPolicy(@NonNull Object audioPolicy) {
    Preconditions.checkNotNull(audioPolicy, "Illegal null AudioPolicy argument");
    AudioPolicy policy = (AudioPolicy) audioPolicy;
    String id = getIdForAudioPolicy(audioPolicy);
    if (registeredAudioPolicies.containsKey(id)) {
      return AudioManager.ERROR;
    }
    registeredAudioPolicies.put(id, policy);
    policy.setRegistration(id);
    return AudioManager.SUCCESS;
  }

  @HiddenApi
  @Implementation(minSdk = Q)
  protected void unregisterAudioPolicy(@NonNull Object audioPolicy) {
    Preconditions.checkNotNull(audioPolicy, "Illegal null AudioPolicy argument");
    AudioPolicy policy = (AudioPolicy) audioPolicy;
    registeredAudioPolicies.remove(getIdForAudioPolicy(policy));
    policy.setRegistration(null);
  }

  /**
   * Returns true if at least one audio policy is registered with this manager, and false otherwise.
   */
  public boolean isAnyAudioPolicyRegistered() {
    return !registeredAudioPolicies.isEmpty();
  }

  /**
   * Provides a mock like interface for the {@link AudioManager#generateAudioSessionId} method by
   * returning positive distinct values, or {@link AudioManager#ERROR} if all possible values have
   * already been returned.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected int generateAudioSessionId() {
    if (audioSessionIdCounter < 0) {
      return AudioManager.ERROR;
    }

    return audioSessionIdCounter++;
  }

  private static String getIdForAudioPolicy(@NonNull Object audioPolicy) {
    return Integer.toString(System.identityHashCode(audioPolicy));
  }

  private static void writeMono16BitAudioFormatToParcel(Parcel p) {
    p.writeInt(
        AudioFormat.AUDIO_FORMAT_HAS_PROPERTY_ENCODING
            + AudioFormat.AUDIO_FORMAT_HAS_PROPERTY_SAMPLE_RATE
            + AudioFormat.AUDIO_FORMAT_HAS_PROPERTY_CHANNEL_MASK); // mPropertySetMask
    p.writeInt(AudioFormat.ENCODING_PCM_16BIT); // mEncoding
    p.writeInt(16000); // mSampleRate
    p.writeInt(AudioFormat.CHANNEL_OUT_MONO); // mChannelMask
    p.writeInt(0); // mChannelIndexMask
  }

  public static class AudioFocusRequest {
    public final AudioManager.OnAudioFocusChangeListener listener;
    public final int streamType;
    public final int durationHint;
    public final android.media.AudioFocusRequest audioFocusRequest;

    private AudioFocusRequest(
        AudioManager.OnAudioFocusChangeListener listener, int streamType, int durationHint) {
      this.listener = listener;
      this.streamType = streamType;
      this.durationHint = durationHint;
      this.audioFocusRequest = null;
    }

    private AudioFocusRequest(android.media.AudioFocusRequest audioFocusRequest) {
      this.listener = audioFocusRequest.getOnAudioFocusChangeListener();
      this.durationHint = audioFocusRequest.getFocusGain();
      this.streamType = audioFocusRequest.getAudioAttributes().getVolumeControlStream();
      this.audioFocusRequest = audioFocusRequest;
    }
  }

  private static class AudioStream {
    private int currentVolume;
    private int maxVolume;
    private int flag;

    public AudioStream(int currVol, int maxVol, int flag) {
      if (MIN_VOLUME > maxVol) {
        throw new IllegalArgumentException("Min volume is higher than max volume.");
      }
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
      } else if (vol < MIN_VOLUME) {
        vol = MIN_VOLUME;
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

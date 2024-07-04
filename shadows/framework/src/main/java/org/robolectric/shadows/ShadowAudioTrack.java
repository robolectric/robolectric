package org.robolectric.shadows;

import static android.media.AudioTrack.ERROR_BAD_VALUE;
import static android.media.AudioTrack.ERROR_DEAD_OBJECT;
import static android.media.AudioTrack.WRITE_BLOCKING;
import static android.media.AudioTrack.WRITE_NON_BLOCKING;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresApi;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioRouting.OnRoutingChangedListener;
import android.media.AudioTrack;
import android.media.AudioTrack.WriteMode;
import android.media.PlaybackParams;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * Implementation of a couple methods in {@link AudioTrack}. Only a couple methods are supported,
 * other methods are expected run through the real class. The two {@link WriteMode} are treated the
 * same.
 */
@Implements(value = AudioTrack.class, looseSignatures = true)
public class ShadowAudioTrack {

  /**
   * Listeners to be notified when data is written to an {@link AudioTrack} via {@link
   * AudioTrack#write(ByteBuffer, int, int)}
   *
   * <p>Currently, only the data written through AudioTrack.write(ByteBuffer audioData, int
   * sizeInBytes, int writeMode) will be reported.
   */
  public interface OnAudioDataWrittenListener {

    /**
     * Called when data is written to {@link ShadowAudioTrack}.
     *
     * @param audioTrack The {@link ShadowAudioTrack} to which the data is written.
     * @param audioData The data that is written to the {@link ShadowAudioTrack}.
     * @param format The output format of the {@link ShadowAudioTrack}.
     */
    void onAudioDataWritten(ShadowAudioTrack audioTrack, byte[] audioData, AudioFormat format);
  }

  protected static final int DEFAULT_MIN_BUFFER_SIZE = 1024;

  // Copied from native code
  // https://cs.android.com/android/platform/superproject/+/android13-release:frameworks/base/core/jni/android_media_AudioTrack.cpp?q=AUDIOTRACK_ERROR_SETUP_NATIVEINITFAILED
  private static final int AUDIOTRACK_ERROR_SETUP_NATIVEINITFAILED = -20;

  private static final String TAG = "ShadowAudioTrack";

  /** Direct playback support checked from {@link #native_is_direct_output_supported}. */
  private static final Multimap<AudioFormatInfo, AudioAttributesInfo> directSupportedFormats =
      Multimaps.synchronizedMultimap(HashMultimap.create());

  /** Non-PCM encodings allowed for creating an AudioTrack instance. */
  private static final Set<Integer> allowedNonPcmEncodings =
      Collections.synchronizedSet(new HashSet<>());

  private static AudioDeviceInfo routedDevice;
  private static final Set<OnRoutingChangedListenerInfo> onRoutingChangedListeners =
      new CopyOnWriteArraySet<>();
  private static final List<OnAudioDataWrittenListener> audioDataWrittenListeners =
      new CopyOnWriteArrayList<>();
  private static int minBufferSize = DEFAULT_MIN_BUFFER_SIZE;

  private int numBytesReceived;
  private PlaybackParams playbackParams;
  @RealObject AudioTrack audioTrack;

  /**
   * In the real class, the minimum buffer size is estimated from audio sample rate and other
   * factors. We do not provide such estimation in {@link #native_get_min_buff_size(int, int, int)},
   * instead letting users set the minimum for the expected audio sample. Usually higher sample rate
   * requires bigger buffer size.
   */
  public static void setMinBufferSize(int bufferSize) {
    minBufferSize = bufferSize;
  }

  /**
   * Adds support for direct playback for the pair of {@link AudioFormat} and {@link
   * AudioAttributes} where the format encoding must be non-PCM. Calling {@link
   * AudioTrack#isDirectPlaybackSupported(AudioFormat, AudioAttributes)} will return {@code true}
   * for matching {@link AudioFormat} and {@link AudioAttributes}. The matching is performed against
   * the format's {@linkplain AudioFormat#getEncoding() encoding}, {@linkplain
   * AudioFormat#getSampleRate() sample rate}, {@linkplain AudioFormat#getChannelMask() channel
   * mask} and {@linkplain AudioFormat#getChannelIndexMask() channel index mask}, and the
   * attribute's {@linkplain AudioAttributes#getContentType() content type}, {@linkplain
   * AudioAttributes#getUsage() usage} and {@linkplain AudioAttributes#getFlags() flags}.
   *
   * @param format The {@link AudioFormat}, which must be of a non-PCM encoding. If the encoding is
   *     PCM, the method will throw an {@link IllegalArgumentException}.
   * @param attr The {@link AudioAttributes}.
   */
  public static void addDirectPlaybackSupport(
      @NonNull AudioFormat format, @NonNull AudioAttributes attr) {
    checkNotNull(format);
    checkNotNull(attr);
    checkArgument(!isPcm(format.getEncoding()));

    directSupportedFormats.put(
        new AudioFormatInfo(
            format.getEncoding(),
            format.getSampleRate(),
            format.getChannelMask(),
            format.getChannelIndexMask()),
        new AudioAttributesInfo(attr.getContentType(), attr.getUsage(), attr.getFlags()));
  }

  /**
   * Clears all encodings that have been added for direct playback support with {@link
   * #addDirectPlaybackSupport}.
   */
  public static void clearDirectPlaybackSupportedFormats() {
    directSupportedFormats.clear();
  }

  /**
   * Add a non-PCM encoding for which {@link AudioTrack} instances are allowed to be created.
   *
   * @param encoding One of {@link AudioFormat} {@code ENCODING_} constants that represents a
   *     non-PCM encoding. If {@code encoding} is PCM, this method throws an {@link
   *     IllegalArgumentException}.
   */
  public static void addAllowedNonPcmEncoding(int encoding) {
    checkArgument(!isPcm(encoding));
    allowedNonPcmEncodings.add(encoding);
  }

  /** Clears all encodings that have been added with {@link #addAllowedNonPcmEncoding(int)}. */
  public static void clearAllowedNonPcmEncodings() {
    allowedNonPcmEncodings.clear();
  }

  /**
   * Sets the routed device returned from {@link AudioTrack#getRoutedDevice()} and informs all
   * registered {@link OnRoutingChangedListener}.
   *
   * <p>Note that this affects the routed device for all {@link AudioTrack} instances.
   *
   * @param routedDevice The route device, or null to reset it to unknown.
   */
  @RequiresApi(N)
  public static void setRoutedDevice(@Nullable AudioDeviceInfo routedDevice) {
    if (Objects.equals(routedDevice, ShadowAudioTrack.routedDevice)) {
      return;
    }
    ShadowAudioTrack.routedDevice = routedDevice;
    for (OnRoutingChangedListenerInfo listenerInfo : onRoutingChangedListeners) {
      listenerInfo.callListener();
    }
  }

  @Implementation(minSdk = N, maxSdk = P)
  protected static int native_get_FCC_8() {
    // Return the value hard-coded in native code:
    // https://cs.android.com/android/platform/superproject/+/android-7.1.1_r41:system/media/audio/include/system/audio.h;l=42;drc=57a4158dc4c4ce62bc6a2b8a0072ba43305548d4
    return 8;
  }

  @Implementation(minSdk = Q)
  protected static boolean native_is_direct_output_supported(
      int encoding,
      int sampleRate,
      int channelMask,
      int channelIndexMask,
      int contentType,
      int usage,
      int flags) {
    return directSupportedFormats.containsEntry(
        new AudioFormatInfo(encoding, sampleRate, channelMask, channelIndexMask),
        new AudioAttributesInfo(contentType, usage, flags));
  }

  /** Returns a predefined or default minimum buffer size. Audio format and config are neglected. */
  @Implementation
  protected static int native_get_min_buff_size(
      int sampleRateInHz, int channelConfig, int audioFormat) {
    return minBufferSize;
  }

  @Implementation(minSdk = P, maxSdk = Q)
  protected int native_setup(
      Object /*WeakReference<AudioTrack>*/ audioTrack,
      Object /*AudioAttributes*/ attributes,
      int[] sampleRate,
      int channelMask,
      int channelIndexMask,
      int audioFormat,
      int buffSizeInBytes,
      int mode,
      int[] sessionId,
      long nativeAudioTrack,
      boolean offload) {
    // If offload, AudioTrack.Builder.build() has checked offload support via AudioSystem.
    if (!offload && !isPcm(audioFormat) && !allowedNonPcmEncodings.contains(audioFormat)) {
      return AUDIOTRACK_ERROR_SETUP_NATIVEINITFAILED;
    }
    return AudioTrack.SUCCESS;
  }

  @Implementation(minSdk = R, maxSdk = R)
  protected int native_setup(
      Object /*WeakReference<AudioTrack>*/ audioTrack,
      Object /*AudioAttributes*/ attributes,
      int[] sampleRate,
      int channelMask,
      int channelIndexMask,
      int audioFormat,
      int buffSizeInBytes,
      int mode,
      int[] sessionId,
      long nativeAudioTrack,
      boolean offload,
      int encapsulationMode,
      Object tunerConfiguration) {
    // If offload, AudioTrack.Builder.build() has checked offload support via AudioSystem.
    if (!offload && !isPcm(audioFormat) && !allowedNonPcmEncodings.contains(audioFormat)) {
      return AUDIOTRACK_ERROR_SETUP_NATIVEINITFAILED;
    }
    return AudioTrack.SUCCESS;
  }

  @Implementation(minSdk = S, maxSdk = TIRAMISU)
  protected int native_setup(
      Object /*WeakReference<AudioTrack>*/ audioTrack,
      Object /*AudioAttributes*/ attributes,
      int[] sampleRate,
      int channelMask,
      int channelIndexMask,
      int audioFormat,
      int buffSizeInBytes,
      int mode,
      int[] sessionId,
      long nativeAudioTrack,
      boolean offload,
      int encapsulationMode,
      Object tunerConfiguration,
      String opPackageName) {
    // If offload, AudioTrack.Builder.build() has checked offload support via AudioSystem.
    if (!offload && !isPcm(audioFormat) && !allowedNonPcmEncodings.contains(audioFormat)) {
      return AUDIOTRACK_ERROR_SETUP_NATIVEINITFAILED;
    }
    return AudioTrack.SUCCESS;
  }

  @Implementation(minSdk = U.SDK_INT)
  protected int native_setup(
      Object /*WeakReference<AudioTrack>*/ audioTrack,
      Object /*AudioAttributes*/ attributes,
      int[] sampleRate,
      int channelMask,
      int channelIndexMask,
      int audioFormat,
      int buffSizeInBytes,
      int mode,
      int[] sessionId,
      @NonNull Parcel attributionSource,
      long nativeAudioTrack,
      boolean offload,
      int encapsulationMode,
      Object tunerConfiguration,
      @NonNull String opPackageName) {
    // If offload, AudioTrack.Builder.build() has checked offload support via AudioSystem.
    if (!offload && !isPcm(audioFormat) && !allowedNonPcmEncodings.contains(audioFormat)) {
      return AUDIOTRACK_ERROR_SETUP_NATIVEINITFAILED;
    }
    return AudioTrack.SUCCESS;
  }

  /**
   * Returns the number of bytes to write. This method returns immediately even with {@link
   * AudioTrack#WRITE_BLOCKING}. If the {@link AudioTrack} instance was created with a non-PCM
   * encoding and the encoding can no longer be played directly, the method will return {@link
   * AudioTrack#ERROR_DEAD_OBJECT};
   */
  @Implementation(minSdk = M)
  protected int native_write_byte(
      byte[] audioData, int offsetInBytes, int sizeInBytes, int format, boolean isBlocking) {
    int encoding = audioTrack.getAudioFormat();
    // Assume that offload support does not change during the lifetime of the instance.
    if ((VERSION.SDK_INT < 29 || !audioTrack.isOffloadedPlayback())
        && !isPcm(encoding)
        && !allowedNonPcmEncodings.contains(encoding)) {
      return ERROR_DEAD_OBJECT;
    }
    return sizeInBytes;
  }

  @Implementation(minSdk = N)
  protected AudioDeviceInfo getRoutedDevice() {
    return routedDevice;
  }

  @Implementation(minSdk = N)
  protected void addOnRoutingChangedListener(
      @NonNull OnRoutingChangedListener listener, Handler handler) {
    OnRoutingChangedListenerInfo listenerInfo =
        new OnRoutingChangedListenerInfo(listener, audioTrack, handler);
    onRoutingChangedListeners.add(listenerInfo);
    if (routedDevice != null) {
      listenerInfo.callListener();
    }
  }

  @Implementation(minSdk = N)
  protected void removeOnRoutingChangedListener(@NonNull OnRoutingChangedListener listener) {
    onRoutingChangedListeners.removeIf(
        registeredListener -> registeredListener.listener.equals(listener));
  }

  @Implementation(minSdk = M)
  public void setPlaybackParams(@NonNull PlaybackParams params) {
    playbackParams = checkNotNull(params, "Illegal null params");
  }

  @Implementation(minSdk = M)
  @NonNull
  protected PlaybackParams getPlaybackParams() {
    return playbackParams;
  }

  /**
   * Returns the number of bytes to write, except with invalid parameters. If the {@link AudioTrack}
   * was created for a non-PCM encoding that can no longer be played directly, it returns {@link
   * AudioTrack#ERROR_DEAD_OBJECT}. Assumes {@link AudioTrack} is already initialized (object
   * properly created). Do not block even if {@link AudioTrack} in offload mode is in STOPPING play
   * state. This method returns immediately even with {@link AudioTrack#WRITE_BLOCKING}
   */
  @Implementation
  protected int write(@NonNull ByteBuffer audioData, int sizeInBytes, @WriteMode int writeMode) {
    int encoding = audioTrack.getAudioFormat();
    // Assume that offload support does not change during the lifetime of the instance.
    if ((VERSION.SDK_INT < 29 || !audioTrack.isOffloadedPlayback())
        && !isPcm(encoding)
        && !allowedNonPcmEncodings.contains(encoding)) {
      return ERROR_DEAD_OBJECT;
    }
    if (writeMode != WRITE_BLOCKING && writeMode != WRITE_NON_BLOCKING) {
      Log.e(TAG, "ShadowAudioTrack.write() called with invalid blocking mode");
      return ERROR_BAD_VALUE;
    }
    if (sizeInBytes < 0 || sizeInBytes > audioData.remaining()) {
      Log.e(TAG, "ShadowAudioTrack.write() called with invalid size (" + sizeInBytes + ") value");
      return ERROR_BAD_VALUE;
    }

    byte[] receivedBytes = new byte[sizeInBytes];
    audioData.get(receivedBytes);
    numBytesReceived += sizeInBytes;

    for (OnAudioDataWrittenListener listener : audioDataWrittenListeners) {
      listener.onAudioDataWritten(this, receivedBytes, audioTrack.getFormat());
    }

    return sizeInBytes;
  }

  @Implementation
  protected int getPlaybackHeadPosition() {
    return numBytesReceived / audioTrack.getFormat().getFrameSizeInBytes();
  }

  @Implementation
  protected void flush() {
    numBytesReceived = 0;
  }

  /**
   * Registers an {@link OnAudioDataWrittenListener} to the {@link ShadowAudioTrack}.
   *
   * @param listener The {@link OnAudioDataWrittenListener} to be registered.
   */
  public static void addAudioDataListener(OnAudioDataWrittenListener listener) {
    ShadowAudioTrack.audioDataWrittenListeners.add(listener);
  }

  /**
   * Removes an {@link OnAudioDataWrittenListener} from the {@link ShadowAudioTrack}.
   *
   * @param listener The {@link OnAudioDataWrittenListener} to be removed.
   */
  public static void removeAudioDataListener(OnAudioDataWrittenListener listener) {
    ShadowAudioTrack.audioDataWrittenListeners.remove(listener);
  }

  @Resetter
  public static void resetTest() {
    audioDataWrittenListeners.clear();
    clearDirectPlaybackSupportedFormats();
    clearAllowedNonPcmEncodings();
    routedDevice = null;
  }

  private static boolean isPcm(int encoding) {
    switch (encoding) {
      case AudioFormat.ENCODING_PCM_8BIT:
      case AudioFormat.ENCODING_PCM_16BIT:
      case AudioFormat.ENCODING_PCM_24BIT_PACKED:
      case AudioFormat.ENCODING_PCM_32BIT:
      case AudioFormat.ENCODING_PCM_FLOAT:
        return true;
      default:
        return false;
    }
  }

  /**
   * Specific fields from {@link AudioFormat} that are used for detection of direct playback
   * support.
   *
   * @see #native_is_direct_output_supported
   */
  private static class AudioFormatInfo {
    private final int encoding;
    private final int sampleRate;
    private final int channelMask;
    private final int channelIndexMask;

    public AudioFormatInfo(int encoding, int sampleRate, int channelMask, int channelIndexMask) {
      this.encoding = encoding;
      this.sampleRate = sampleRate;
      this.channelMask = channelMask;
      this.channelIndexMask = channelIndexMask;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AudioFormatInfo)) {
        return false;
      }

      AudioFormatInfo other = (AudioFormatInfo) o;
      return encoding == other.encoding
          && sampleRate == other.sampleRate
          && channelMask == other.channelMask
          && channelIndexMask == other.channelIndexMask;
    }

    @Override
    public int hashCode() {
      int result = encoding;
      result = 31 * result + sampleRate;
      result = 31 * result + channelMask;
      result = 31 * result + channelIndexMask;
      return result;
    }
  }

  /**
   * Specific fields from {@link AudioAttributes} used for detection of direct playback support.
   *
   * @see #native_is_direct_output_supported
   */
  private static class AudioAttributesInfo {
    private final int contentType;
    private final int usage;
    private final int flags;

    public AudioAttributesInfo(int contentType, int usage, int flags) {
      this.contentType = contentType;
      this.usage = usage;
      this.flags = flags;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AudioAttributesInfo)) {
        return false;
      }

      AudioAttributesInfo other = (AudioAttributesInfo) o;
      return contentType == other.contentType && usage == other.usage && flags == other.flags;
    }

    @Override
    public int hashCode() {
      int result = contentType;
      result = 31 * result + usage;
      result = 31 * result + flags;
      return result;
    }
  }

  private static final class OnRoutingChangedListenerInfo {
    private final OnRoutingChangedListener listener;
    private final AudioTrack audioTrack;
    private final Handler handler;

    public OnRoutingChangedListenerInfo(
        OnRoutingChangedListener listener, AudioTrack audioTrack, Handler handler) {
      this.listener = listener;
      this.audioTrack = audioTrack;
      this.handler = handler;
    }

    public void callListener() {
      handler.post(() -> listener.onRoutingChanged(audioTrack));
    }
  }
}

package org.robolectric.shadows;

import static android.media.AudioTrack.ERROR_BAD_VALUE;
import static android.media.AudioTrack.ERROR_DEAD_OBJECT;
import static android.media.AudioTrack.WRITE_BLOCKING;
import static android.media.AudioTrack.WRITE_NON_BLOCKING;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.base.Preconditions.checkNotNull;

import android.annotation.NonNull;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioTrack.WriteMode;
import android.media.PlaybackParams;
import android.os.Parcel;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

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
  private static final Set<Integer> directSupportedEncodings =
      Collections.synchronizedSet(new HashSet<>());

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
   * Adds support for direct playback of a non-PCM {@code encoding}. As a result, calling {@link
   * AudioTrack#isDirectPlaybackSupported(AudioFormat, AudioAttributes)} for an {@link AudioFormat}
   * of the same encoding, will return {@code true}. The {@link AudioAttributes} are ignored.
   *
   * @param encoding One of {@link AudioFormat} {@code ENCODING_} values excluding PCM encodings. If
   *     {@code encoding} is PCM, the method will throw an {@link IllegalArgumentException}.
   */
  public static void addDirectPlaybackSupport(int encoding) {
    if (isPcm(encoding)) {
      throw new IllegalArgumentException("Encoding is PCM: " + encoding);
    }
    directSupportedEncodings.add(encoding);
  }

  /**
   * Clears all encodings that have been added for direct playback support with {@link
   * #addDirectPlaybackSupport(int)}.
   */
  public static void clearDirectPlaybackSupportedEncodings() {
    directSupportedEncodings.clear();
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
    return directSupportedEncodings.contains(encoding);
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
    if (!isPcm(audioFormat) && !directSupportedEncodings.contains(audioFormat)) {
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
    if (!isPcm(audioFormat) && !directSupportedEncodings.contains(audioFormat)) {
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
    if (!isPcm(audioFormat) && !directSupportedEncodings.contains(audioFormat)) {
      return AUDIOTRACK_ERROR_SETUP_NATIVEINITFAILED;
    }
    return AudioTrack.SUCCESS;
  }

  @Implementation(minSdk = ShadowBuild.UPSIDE_DOWN_CAKE)
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
    if (!isPcm(audioFormat) && !directSupportedEncodings.contains(audioFormat)) {
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
  protected final int native_write_byte(
      byte[] audioData, int offsetInBytes, int sizeInBytes, int format, boolean isBlocking) {
    int encoding = audioTrack.getAudioFormat();
    if (!isPcm(encoding) && !directSupportedEncodings.contains(encoding)) {
      return ERROR_DEAD_OBJECT;
    }
    return sizeInBytes;
  }

  /**
   * Returns the number of bytes to write. This method returns immediately even with {@link
   * AudioTrack#WRITE_BLOCKING}. If the {@link AudioTrack} instance was created with a non-PCM
   * encoding and the encoding can no longer be played directly, the method will return {@link
   * AudioTrack#ERROR_DEAD_OBJECT};
   */
  @Implementation(minSdk = Q)
  protected int native_write_native_bytes(
      ByteBuffer audioData, int positionInBytes, int sizeInBytes, int format, boolean blocking) {
    int encoding = audioTrack.getAudioFormat();
    if (!isPcm(encoding) && !directSupportedEncodings.contains(encoding)) {
      return ERROR_DEAD_OBJECT;
    }
    return sizeInBytes;
  }

  @Implementation(minSdk = M)
  public void setPlaybackParams(@NonNull PlaybackParams params) {
    playbackParams = checkNotNull(params, "Illegal null params");
  }

  @Implementation(minSdk = M)
  @NonNull
  protected final PlaybackParams getPlaybackParams() {
    return playbackParams;
  }

  /**
   * Returns the number of bytes to write, except with invalid parameters. If the {@link AudioTrack}
   * was created for a non-PCM encoding that can no longer be played directly, it returns {@link
   * AudioTrack#ERROR_DEAD_OBJECT}. Assumes {@link AudioTrack} is already initialized (object
   * properly created). Do not block even if {@link AudioTrack} in offload mode is in STOPPING play
   * state. This method returns immediately even with {@link AudioTrack#WRITE_BLOCKING}
   */
  @Implementation(minSdk = LOLLIPOP)
  protected int write(@NonNull ByteBuffer audioData, int sizeInBytes, @WriteMode int writeMode) {
    int encoding = audioTrack.getAudioFormat();
    if (!isPcm(encoding) && !directSupportedEncodings.contains(encoding)) {
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
    clearDirectPlaybackSupportedEncodings();
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
}

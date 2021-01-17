package org.robolectric.shadows;

import static android.media.AudioTrack.ERROR_BAD_VALUE;
import static android.media.AudioTrack.WRITE_BLOCKING;
import static android.media.AudioTrack.WRITE_NON_BLOCKING;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;

import android.annotation.NonNull;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioTrack.WriteMode;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.List;
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

  private static final String TAG = "ShadowAudioTrack";
  private static int minBufferSize = DEFAULT_MIN_BUFFER_SIZE;
  private static final List<OnAudioDataWrittenListener> audioDataWrittenListeners =
      new CopyOnWriteArrayList<>();
  private int numBytesReceived;
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

  @Implementation(minSdk = N, maxSdk = P)
  protected static int native_get_FCC_8() {
    // Return the value hard-coded in native code:
    // https://cs.android.com/android/platform/superproject/+/android-7.1.1_r41:system/media/audio/include/system/audio.h;l=42;drc=57a4158dc4c4ce62bc6a2b8a0072ba43305548d4
    return 8;
  }

  /** Returns a predefined or default minimum buffer size. Audio format and config are neglected. */
  @Implementation
  protected static int native_get_min_buff_size(
      int sampleRateInHz, int channelConfig, int audioFormat) {
    return minBufferSize;
  }

  /**
   * Always return the number of bytes to write. This method returns immedidately even with {@link
   * AudioTrack#WRITE_BLOCKING}
   */
  @Implementation(minSdk = M)
  protected final int native_write_byte(
      byte[] audioData, int offsetInBytes, int sizeInBytes, int format, boolean isBlocking) {
    return sizeInBytes;
  }

  /**
   * Always return the number of bytes to write except with invalid parameters. Assumes AudioTrack
   * is already initialized (object properly created). Do not block even if AudioTrack in offload
   * mode is in STOPPING play state. This method returns immediately even with {@link
   * AudioTrack#WRITE_BLOCKING}
   */
  @Implementation(minSdk = LOLLIPOP)
  protected int write(@NonNull ByteBuffer audioData, int sizeInBytes, @WriteMode int writeMode) {
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
  }
}

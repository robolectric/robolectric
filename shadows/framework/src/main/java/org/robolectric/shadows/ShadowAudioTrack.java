package org.robolectric.shadows;

import static android.media.AudioTrack.ERROR_BAD_VALUE;
import static android.media.AudioTrack.WRITE_BLOCKING;
import static android.media.AudioTrack.WRITE_NON_BLOCKING;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.media.AudioTrack;
import android.media.AudioTrack.WriteMode;
import androidx.annotation.NonNull;
import android.util.Log;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Implementation of a couple methods in {@link AudioTrack}. Only a couple methods are supported,
 * other methods are expected run through the real class. The two {@link WriteMode} are treated the
 * same.
 */
@Implements(value = AudioTrack.class, looseSignatures = true)
public class ShadowAudioTrack {

  protected static final int DEFAULT_MIN_BUFFER_SIZE = 1024;

  private static final String TAG = "ShadowAudioTrack";
  private static int minBufferSize = DEFAULT_MIN_BUFFER_SIZE;

  /**
   * In the real class, the minimum buffer size is estimated from audio sample rate and other
   * factors. We do not provide such estimation in {@link #native_get_min_buff_size(int, int, int)},
   * instead letting users set the minimum for the expected audio sample. Usually higher sample rate
   * requires bigger buffer size.
   */
  public static void setMinBufferSize(int bufferSize) {
    minBufferSize = bufferSize;
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
    ((Buffer) audioData).position(audioData.position() + sizeInBytes);
    return sizeInBytes;
  }
}

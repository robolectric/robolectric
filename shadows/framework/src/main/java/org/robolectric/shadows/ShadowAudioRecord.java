package org.robolectric.shadows;

import static android.media.AudioRecord.ERROR_BAD_VALUE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioSystem;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

/**
 * Shadow {@link AudioRecord} which by default will fulfil any requests for audio data by completely
 * filling any requested buffers.
 *
 * <p>It is also possible to provide the underlying data by implementing {@link AudioRecordSource}
 * and setting this via {@link #setSourceProvider(AudioRecordSourceProvider)}.
 */
@Implements(value = AudioRecord.class, minSdk = LOLLIPOP)
public final class ShadowAudioRecord {

  @RealObject AudioRecord audioRecord;

  private static final AudioRecordSource DEFAULT_SOURCE = new AudioRecordSource() {};
  private static final AtomicReference<AudioRecordSourceProvider> audioRecordSourceProvider =
      new AtomicReference<>(audioRecord -> DEFAULT_SOURCE);

  /**
   * Sets {@link AudioRecordSource} to be used for providing data to {@link AudioRecord}.
   *
   * @deprecated use {@link #setSourceProvider(AudioRecordSourceProvider)}.
   */
  @Deprecated
  public static void setSource(AudioRecordSource source) {
    audioRecordSourceProvider.set(audioRecord -> source);
  }

  /**
   * Sets {@link AudioRecordSourceProvider} to be used for providing data of {@link AudioRecord}.
   */
  public static void setSourceProvider(AudioRecordSourceProvider audioRecordSourceProvider) {
    ShadowAudioRecord.audioRecordSourceProvider.set(audioRecordSourceProvider);
  }

  /**
   * Resets {@link AudioRecordSource} to be used for providing data to {@link AudioRecord}, so that
   * all requests are fulfilled for audio data by completely filling any requested buffers.
   */
  @Resetter
  public static void clearSource() {
    setSource(DEFAULT_SOURCE);
  }

  @Implementation
  protected static int native_get_min_buff_size(
      int sampleRateInHz, int channelCount, int audioFormat) {
    int frameSize;
    switch (audioFormat) {
      case AudioFormat.ENCODING_PCM_16BIT:
        frameSize = 2;
        break;
      case AudioFormat.ENCODING_PCM_FLOAT:
        frameSize = 2 * channelCount;
        break;
      default:
        return ERROR_BAD_VALUE;
    }
    return frameSize * (sampleRateInHz / 4); // Approx quarter of a second sample per buffer
  }

  @Implementation
  protected int native_start(int syncEvent, int sessionId) {
    return AudioSystem.SUCCESS;
  }

  @Implementation(maxSdk = LOLLIPOP_MR1)
  protected int native_read_in_byte_array(byte[] audioData, int offsetInBytes, int sizeInBytes) {
    return native_read_in_byte_array(audioData, offsetInBytes, sizeInBytes, true);
  }

  @Implementation(minSdk = M)
  protected int native_read_in_byte_array(
      byte[] audioData, int offsetInBytes, int sizeInBytes, boolean isBlocking) {
    return getAudioRecordSource()
        .readInByteArray(audioData, offsetInBytes, sizeInBytes, isBlocking);
  }

  @Implementation(maxSdk = LOLLIPOP_MR1)
  protected int native_read_in_short_array(
      short[] audioData, int offsetInShorts, int sizeInShorts) {
    return native_read_in_short_array(audioData, offsetInShorts, sizeInShorts, true);
  }

  @Implementation(minSdk = M)
  protected int native_read_in_short_array(
      short[] audioData, int offsetInShorts, int sizeInShorts, boolean isBlocking) {
    return getAudioRecordSource()
        .readInShortArray(audioData, offsetInShorts, sizeInShorts, isBlocking);
  }

  @Implementation(minSdk = M)
  protected int native_read_in_float_array(
      float[] audioData, int offsetInFloats, int sizeInFloats, boolean isBlocking) {
    return getAudioRecordSource()
        .readInFloatArray(audioData, offsetInFloats, sizeInFloats, isBlocking);
  }

  @Implementation(maxSdk = LOLLIPOP_MR1)
  protected int native_read_in_direct_buffer(Object jBuffer, int sizeInBytes) {
    return native_read_in_direct_buffer(jBuffer, sizeInBytes, true);
  }

  @Implementation(minSdk = M)
  protected int native_read_in_direct_buffer(Object jBuffer, int sizeInBytes, boolean isBlocking) {
    // Note, in the real implementation the buffers position is not adjusted during the
    // read, so use duplicate to ensure the real implementation is matched.
    return getAudioRecordSource()
        .readInDirectBuffer(((ByteBuffer) jBuffer).duplicate(), sizeInBytes, isBlocking);
  }

  private AudioRecordSource getAudioRecordSource() {
    return audioRecordSourceProvider.get().get(audioRecord);
  }

  /** Provides {@link AudioRecordSource} for the given {@link AudioRecord}. */
  public interface AudioRecordSourceProvider {
    AudioRecordSource get(AudioRecord audioRecord);
  }

  /** Provides underlying data for the {@link ShadowAudioRecord}. */
  public interface AudioRecordSource {

    /**
     * Provides backing data for {@link AudioRecord#read(byte[], int, int)} and {@link
     * AudioRecord#read(byte[], int, int, int)}.
     *
     * @return Either a non-negative value representing number of bytes that have been written from
     *     the offset or a negative error code.
     */
    default int readInByteArray(
        byte[] audioData, int offsetInBytes, int sizeInBytes, boolean isBlocking) {
      return sizeInBytes;
    }

    /**
     * Provides backing data for {@link AudioRecord#read(short[], int, int)} and {@link
     * AudioRecord#read(short[], int, int, int)}.
     *
     * @return Either a non-negative value representing number of bytes that have been written from
     *     the offset or a negative error code.
     */
    default int readInShortArray(
        short[] audioData, int offsetInShorts, int sizeInShorts, boolean isBlocking) {
      return sizeInShorts;
    }

    /**
     * Provides backing data for {@link AudioRecord#read(float[], int, int, int)}.
     *
     * @return Either a non-negative value representing number of bytes that have been written from
     *     the offset or a negative error code.
     */
    default int readInFloatArray(
        float[] audioData, int offsetInFloats, int sizeInFloats, boolean isBlocking) {
      return sizeInFloats;
    }

    /**
     * Provides backing data for {@link AudioRecord#read(byte[], int, int)} and {@link
     * AudioRecord#read(byte[], int, int, int)}.
     *
     * @return Either a non-negative value representing number of bytes that have been written from
     *     the offset or a negative error code. Note any position/limit changes to the buffer will
     *     not be visible to the caller of the AudioRecord methods.
     */
    default int readInDirectBuffer(ByteBuffer buffer, int sizeInBytes, boolean isBlocking) {
      int maxBytes = Math.min(buffer.remaining(), sizeInBytes);
      ((Buffer) buffer).position(buffer.position() + maxBytes);
      return maxBytes;
    }
  }
}

package org.robolectric.shadows;

import static android.media.AudioTrack.ERROR_BAD_VALUE;
import static android.media.AudioTrack.WRITE_BLOCKING;
import static android.media.AudioTrack.WRITE_NON_BLOCKING;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowAudioTrack}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowAudioTrackTest implements ShadowAudioTrack.OnAudioDataWrittenListener {

  private static final int SAMPLE_RATE_IN_HZ = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
  private static final int AUDIO_ENCODING_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  private ShadowAudioTrack shadowAudioTrack;
  private byte[] dataWrittenToShadowAudioTrack;

  @After
  public void tearDown() {
    // Clean up any static state changes.
    ShadowAudioTrack.clearDirectPlaybackSupportedEncodings();
  }

  @Test
  public void multichannelAudio_isSupported() {
    AudioFormat format =
        new AudioFormat.Builder()
            .setChannelMask(
                AudioFormat.CHANNEL_OUT_FRONT_CENTER
                    | AudioFormat.CHANNEL_OUT_FRONT_LEFT
                    | AudioFormat.CHANNEL_OUT_FRONT_RIGHT
                    | AudioFormat.CHANNEL_OUT_BACK_LEFT
                    | AudioFormat.CHANNEL_OUT_BACK_RIGHT
                    | AudioFormat.CHANNEL_OUT_LOW_FREQUENCY)
            .setEncoding(AUDIO_ENCODING_FORMAT)
            .setSampleRate(SAMPLE_RATE_IN_HZ)
            .build();

    // 2s buffer
    int bufferSizeBytes =
        2 * SAMPLE_RATE_IN_HZ * 6 * AudioFormat.getBytesPerSample(AUDIO_ENCODING_FORMAT);

    // Ensure the constructor doesn't throw an exception.
    new AudioTrack(
        new AudioAttributes.Builder().build(),
        format,
        bufferSizeBytes,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE);
  }

  @Test
  @Config(minSdk = Q)
  public void setMinBufferSize() {
    int originalMinBufferSize =
        AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_ENCODING_FORMAT);
    ShadowAudioTrack.setMinBufferSize(512);
    int newMinBufferSize =
        AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_ENCODING_FORMAT);

    assertThat(originalMinBufferSize).isEqualTo(ShadowAudioTrack.DEFAULT_MIN_BUFFER_SIZE);
    assertThat(newMinBufferSize).isEqualTo(512);
  }

  @Test
  @Config(minSdk = Q)
  public void writeByteArray_blocking() {
    AudioTrack audioTrack = getSampleAudioTrack();

    int written = audioTrack.write(new byte[] {0, 0, 0, 0}, 0, 2);

    assertThat(written).isEqualTo(2);
  }

  @Test
  @Config(minSdk = Q)
  public void writeByteArray_nonBlocking() {
    AudioTrack audioTrack = getSampleAudioTrack();

    int written = audioTrack.write(new byte[] {0, 0, 0, 0}, 0, 2, WRITE_NON_BLOCKING);

    assertThat(written).isEqualTo(2);
  }

  @Test
  @Config(minSdk = Q)
  public void writeByteBuffer_blocking() {
    AudioTrack audioTrack = getSampleAudioTrack();
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);

    int written = audioTrack.write(byteBuffer, 2, WRITE_BLOCKING);

    assertThat(written).isEqualTo(2);
  }

  @Test
  @Config(minSdk = Q)
  public void writeByteBuffer_nonBlocking() {
    AudioTrack audioTrack = getSampleAudioTrack();
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);

    int written = audioTrack.write(byteBuffer, 2, WRITE_NON_BLOCKING);

    assertThat(written).isEqualTo(2);
  }

  @Test
  @Config(minSdk = Q)
  public void writeByteBuffer_correctBytesWritten() {
    ShadowAudioTrack.addAudioDataListener(this);
    AudioTrack audioTrack = getSampleAudioTrack();

    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    byte[] dataToWrite = new byte[] {1, 2, 3, 4};
    byteBuffer.put(dataToWrite);
    byteBuffer.flip();

    audioTrack.write(byteBuffer, 4, WRITE_NON_BLOCKING);

    assertThat(dataWrittenToShadowAudioTrack).isEqualTo(dataToWrite);
    assertThat(shadowAudioTrack.getPlaybackHeadPosition()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = Q)
  public void writeDirectByteBuffer_blocking() {
    AudioTrack audioTrack = getSampleAudioTrack();
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);

    int written = audioTrack.write(byteBuffer, 2, WRITE_BLOCKING);

    assertThat(written).isEqualTo(2);
  }

  @Test
  @Config(minSdk = Q)
  public void writeDirectByteBuffer_nonBlocking() {
    AudioTrack audioTrack = getSampleAudioTrack();
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);

    int written = audioTrack.write(byteBuffer, 2, WRITE_NON_BLOCKING);

    assertThat(written).isEqualTo(2);
  }

  @Test
  @Config(minSdk = Q)
  public void writeDirectByteBuffer_invalidWriteMode() {
    AudioTrack audioTrack = getSampleAudioTrack();
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);

    int written = audioTrack.write(byteBuffer, 2, 5);

    assertThat(written).isEqualTo(ERROR_BAD_VALUE);
  }

  @Test
  @Config(minSdk = Q)
  public void writeDirectByteBuffer_invalidSize() {
    AudioTrack audioTrack = getSampleAudioTrack();
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);

    int written = audioTrack.write(byteBuffer, 10, WRITE_NON_BLOCKING);

    assertThat(written).isEqualTo(ERROR_BAD_VALUE);
  }

  @Test
  public void addDirectPlaybackSupport_forPcmEncoding_throws() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_PCM_8BIT));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_PCM_16BIT));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_PCM_24BIT_PACKED));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_PCM_32BIT));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_PCM_FLOAT));
  }

  @Test
  @Config(minSdk = Q)
  public void isDirectPlaybackSupported() {
    AudioFormat ac3Format = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_AC3).build();
    AudioAttributes audioAttributes = new AudioAttributes.Builder().build();

    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isFalse();

    ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_AC3);

    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void clearDirectPlaybackSupportedEncodings() {
    AudioFormat ac3Format = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_AC3).build();
    AudioAttributes audioAttributes = new AudioAttributes.Builder().build();
    ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_AC3);
    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isTrue();

    ShadowAudioTrack.clearDirectPlaybackSupportedEncodings();

    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void createInstance_withSurroundEncodingButNotSupportedDirectly_throws() {
    AudioFormat surroundAudioFormat =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();

    assertFalse(
        AudioTrack.isDirectPlaybackSupported(
            surroundAudioFormat, new AudioAttributes.Builder().build()));
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            new AudioTrack.Builder()
                .setAudioFormat(surroundAudioFormat)
                .setBufferSizeInBytes(65536)
                .build());
  }

  @Test
  @Config(minSdk = Q)
  public void write_withEncodingSupportedDirectly_succeeds() {
    ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_AC3);

    AudioTrack audioTrack =
        new AudioTrack.Builder()
            .setAudioFormat(
                new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_AC3)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                    .build())
            .setAudioAttributes(new AudioAttributes.Builder().build())
            .setBufferSizeInBytes(32 * 1024)
            .build();

    assertThat(audioTrack.write(new byte[128], 0, 128)).isEqualTo(128);
    assertThat(audioTrack.write(new byte[128], 0, 128, AudioTrack.WRITE_BLOCKING)).isEqualTo(128);
    assertThat(audioTrack.write(ByteBuffer.allocate(128), 128, AudioTrack.WRITE_BLOCKING))
        .isEqualTo(128);
    assertThat(audioTrack.write(ByteBuffer.allocateDirect(128), 128, AudioTrack.WRITE_BLOCKING))
        .isEqualTo(128);
    assertThat(audioTrack.write(ByteBuffer.allocate(128), 128, AudioTrack.WRITE_BLOCKING, 0L))
        .isEqualTo(128);
    assertThat(audioTrack.write(ByteBuffer.allocateDirect(128), 128, AudioTrack.WRITE_BLOCKING, 0L))
        .isEqualTo(128);
  }

  @Test
  @Config(minSdk = Q)
  public void write_withEncodingNoLongerSupportedDirectly_returnsErrorDeadObject() {
    ShadowAudioTrack.addDirectPlaybackSupport(AudioFormat.ENCODING_AC3);
    AudioTrack audioTrack =
        new AudioTrack.Builder()
            .setAudioFormat(
                new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_AC3)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                    .build())
            .setAudioAttributes(new AudioAttributes.Builder().build())
            .setBufferSizeInBytes(32 * 1024)
            .build();

    ShadowAudioTrack.clearDirectPlaybackSupportedEncodings();

    assertThat(audioTrack.write(new byte[128], 0, 128)).isEqualTo(AudioTrack.ERROR_DEAD_OBJECT);
    assertThat(audioTrack.write(new byte[128], 0, 128, AudioTrack.WRITE_BLOCKING))
        .isEqualTo(AudioTrack.ERROR_DEAD_OBJECT);
    assertThat(audioTrack.write(ByteBuffer.allocate(128), 128, AudioTrack.WRITE_BLOCKING))
        .isEqualTo(AudioTrack.ERROR_DEAD_OBJECT);
    assertThat(audioTrack.write(ByteBuffer.allocateDirect(128), 128, AudioTrack.WRITE_BLOCKING))
        .isEqualTo(AudioTrack.ERROR_DEAD_OBJECT);
    assertThat(audioTrack.write(ByteBuffer.allocateDirect(128), 128, AudioTrack.WRITE_BLOCKING, 0L))
        .isEqualTo(AudioTrack.ERROR_DEAD_OBJECT);
  }

  @Override
  @Config(minSdk = Q)
  public void onAudioDataWritten(
      ShadowAudioTrack audioTrack, byte[] audioData, AudioFormat format) {
    shadowAudioTrack = audioTrack;
    dataWrittenToShadowAudioTrack = audioData;
  }

  private static AudioTrack getSampleAudioTrack() {
    return new AudioTrack.Builder()
        .setAudioAttributes(
            new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setAudioFormat(
            new AudioFormat.Builder()
                .setEncoding(AUDIO_ENCODING_FORMAT)
                .setSampleRate(SAMPLE_RATE_IN_HZ)
                .setChannelMask(CHANNEL_CONFIG)
                .build())
        .build();
  }
}

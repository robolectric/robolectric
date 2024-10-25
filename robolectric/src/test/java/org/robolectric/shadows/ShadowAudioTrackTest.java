package org.robolectric.shadows;

import static android.media.AudioTrack.ERROR_BAD_VALUE;
import static android.media.AudioTrack.WRITE_BLOCKING;
import static android.media.AudioTrack.WRITE_NON_BLOCKING;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRouting;
import android.media.AudioRouting.OnRoutingChangedListener;
import android.media.AudioSystem;
import android.media.AudioTrack;
import android.media.PlaybackParams;
import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowAudioTrack}. */
@RunWith(AndroidJUnit4.class)
public class ShadowAudioTrackTest implements ShadowAudioTrack.OnAudioDataWrittenListener {

  private static final int SAMPLE_RATE_IN_HZ = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
  private static final int AUDIO_ENCODING_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
  private ShadowAudioTrack shadowAudioTrack;
  private byte[] dataWrittenToShadowAudioTrack;

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
  @Config(minSdk = M)
  public void getPlaybackParams_withSetPlaybackParams_returnsSetPlaybackParams() {
    PlaybackParams playbackParams =
        new PlaybackParams()
            .allowDefaults()
            .setSpeed(1.0f)
            .setPitch(1.0f)
            .setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_FAIL);
    AudioTrack audioTrack = getSampleAudioTrack();
    audioTrack.setPlaybackParams(playbackParams);

    assertThat(audioTrack.getPlaybackParams()).isEqualTo(playbackParams);
  }

  @Test
  public void addDirectPlaybackSupport_forPcmEncoding_throws() {
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ShadowAudioTrack.addDirectPlaybackSupport(
                getAudioFormat(AudioFormat.ENCODING_PCM_8BIT), attributes));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ShadowAudioTrack.addDirectPlaybackSupport(
                getAudioFormat(AudioFormat.ENCODING_PCM_16BIT), attributes));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ShadowAudioTrack.addDirectPlaybackSupport(
                getAudioFormat(AudioFormat.ENCODING_PCM_24BIT_PACKED), attributes));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ShadowAudioTrack.addDirectPlaybackSupport(
                getAudioFormat(AudioFormat.ENCODING_PCM_32BIT), attributes));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ShadowAudioTrack.addDirectPlaybackSupport(
                getAudioFormat(AudioFormat.ENCODING_PCM_FLOAT), attributes));
  }

  @Test
  @Config(minSdk = Q)
  public void isDirectPlaybackSupported() {
    AudioFormat ac3Format = getAudioFormat(AudioFormat.ENCODING_AC3);
    AudioAttributes audioAttributes = new AudioAttributes.Builder().build();

    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isFalse();

    ShadowAudioTrack.addDirectPlaybackSupport(ac3Format, audioAttributes);

    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void isDirectPlaybackSupported_differentFormatOrAttributeFields() {
    AudioFormat ac3Format = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_AC3).build();
    AudioAttributes audioAttributes = new AudioAttributes.Builder().build();

    ShadowAudioTrack.addDirectPlaybackSupport(ac3Format, audioAttributes);

    assertThat(
            AudioTrack.isDirectPlaybackSupported(
                new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_AC3)
                    .setSampleRate(65000)
                    .build(),
                audioAttributes))
        .isFalse();
    assertThat(
            AudioTrack.isDirectPlaybackSupported(
                ac3Format,
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()))
        .isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void clearDirectPlaybackSupportedEncodings() {
    AudioFormat ac3Format = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_AC3).build();
    AudioAttributes audioAttributes = new AudioAttributes.Builder().build();
    ShadowAudioTrack.addDirectPlaybackSupport(ac3Format, audioAttributes);
    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isTrue();

    ShadowAudioTrack.clearDirectPlaybackSupportedFormats();

    assertThat(AudioTrack.isDirectPlaybackSupported(ac3Format, audioAttributes)).isFalse();
  }

  @Test
  public void addAllowedNonPcmEncoding_forPcmEncoding_throws() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_PCM_8BIT));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_PCM_16BIT));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_PCM_24BIT_PACKED));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_PCM_32BIT));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_PCM_FLOAT));
  }

  @Test
  @Config(minSdk = Q)
  public void createInstance_withNonPcmEncodingNotAllowed_throws() {
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            new AudioTrack.Builder()
                .setAudioFormat(
                    new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_AC3)
                        .setSampleRate(48000)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                        .build())
                .setBufferSizeInBytes(65536)
                .build());
  }

  @Test
  @Config(minSdk = Q)
  public void createInstance_withNonPcmEncodingAllowed() {
    ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_AC3);

    new AudioTrack.Builder()
        .setAudioFormat(
            new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_AC3)
                .setSampleRate(48000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                .build())
        .setBufferSizeInBytes(65536)
        .build();
  }

  @Test
  @Config(minSdk = Q)
  public void createInstance_withOffloadAndEncodingNotOffloaded_throws() {
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            new AudioTrack.Builder()
                .setAudioFormat(
                    new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_AC3)
                        .setSampleRate(48000)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
                        .build())
                .setBufferSizeInBytes(65536)
                .setOffloadedPlayback(true)
                .build());
  }

  @Test
  @Config(minSdk = Q, maxSdk = R)
  public void createInstance_withOffloadAndEncodingIsOffloadSupported() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    ShadowAudioSystem.setOffloadSupported(audioFormat, attributes, /* supported= */ true);

    AudioTrack audioTrack =
        new AudioTrack.Builder()
            .setAudioFormat(audioFormat)
            .setAudioAttributes(attributes)
            .setBufferSizeInBytes(65536)
            .setOffloadedPlayback(true)
            .build();

    assertThat(audioTrack.isOffloadedPlayback()).isTrue();
  }

  @Test
  @Config(sdk = S)
  public void createInstance_withOffloadAndGetOffloadSupport() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    ShadowAudioSystem.setOffloadPlaybackSupport(
        audioFormat, attributes, AudioSystem.OFFLOAD_SUPPORTED);

    AudioTrack audioTrack =
        new AudioTrack.Builder()
            .setAudioFormat(audioFormat)
            .setAudioAttributes(attributes)
            .setBufferSizeInBytes(65536)
            .setOffloadedPlayback(true)
            .build();

    assertThat(audioTrack.isOffloadedPlayback()).isTrue();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void createInstance_withOffloadAndGetDirectPlaybackSupport() {
    AudioFormat audioFormat =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    ShadowAudioSystem.setDirectPlaybackSupport(
        audioFormat, attributes, AudioSystem.OFFLOAD_SUPPORTED);

    AudioTrack audioTrack =
        new AudioTrack.Builder()
            .setAudioFormat(audioFormat)
            .setAudioAttributes(attributes)
            .setBufferSizeInBytes(65536)
            .setOffloadedPlayback(true)
            .build();

    assertThat(audioTrack.isOffloadedPlayback()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void clearAllowedNonPcmEncodings() {
    AudioFormat surroundAudioFormat =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_AC3);
    new AudioTrack.Builder()
        .setAudioFormat(surroundAudioFormat)
        .setBufferSizeInBytes(65536)
        .build();

    ShadowAudioTrack.clearAllowedNonPcmEncodings();

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
  public void write_withNonPcmEncodingSupported_succeeds() {
    ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_AC3);

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
  @Config(minSdk = Q, maxSdk = R)
  public void write_withOffloadUntilApi30_succeeds() {
    ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_AC3);
    AudioFormat ac3Format =
        new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_AC3)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_5POINT1)
            .build();
    AudioAttributes attributes = new AudioAttributes.Builder().build();
    ShadowAudioSystem.setOffloadSupported(ac3Format, attributes, /* supported= */ true);

    AudioTrack audioTrack =
        new AudioTrack.Builder()
            .setAudioFormat(ac3Format)
            .setAudioAttributes(new AudioAttributes.Builder().build())
            .setBufferSizeInBytes(32 * 1024)
            .setOffloadedPlayback(true)
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
  public void write_withNonPcmEncodingNoLongerSupported_returnsErrorDeadObject() {
    ShadowAudioTrack.addAllowedNonPcmEncoding(AudioFormat.ENCODING_AC3);
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

    ShadowAudioTrack.clearAllowedNonPcmEncodings();

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

  @Test
  @Config(minSdk = N)
  public void getRoutedDevice_withoutSetRoutedDevice_returnsNull() {
    AudioTrack audioTrack = new AudioTrack.Builder().build();

    assertThat(audioTrack.getRoutedDevice()).isNull();
  }

  @Test
  @Config(minSdk = N)
  public void getRoutedDevice_afterSetRoutedDevice_returnsRoutedDevice() {
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_HDMI).build();

    ShadowAudioTrack.setRoutedDevice(audioDeviceInfo);

    assertThat(audioTrack.getRoutedDevice()).isEqualTo(audioDeviceInfo);
  }

  @Test
  @Config(minSdk = N)
  public void addOnRoutingChangedListener_beforeSetRoutedDevice_listenerCalledOnceDeviceSet() {
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_HDMI).build();
    AtomicReference<AudioRouting> listenerRouting = new AtomicReference<>();

    audioTrack.addOnRoutingChangedListener(
        (OnRoutingChangedListener) listenerRouting::set, new Handler(Looper.getMainLooper()));
    ShadowLooper.idleMainLooper();

    assertThat(listenerRouting.get()).isNull();

    ShadowAudioTrack.setRoutedDevice(audioDeviceInfo);
    ShadowLooper.idleMainLooper();

    assertThat(listenerRouting.get()).isEqualTo(audioTrack);
    assertThat(listenerRouting.get().getRoutedDevice()).isEqualTo(audioDeviceInfo);
  }

  @Test
  @Config(minSdk = N)
  public void
      addOnRoutingChangedListener_afterSetRoutedDevice_listenerCalledImmediatelyAndWhenNewDeviceSet() {
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    AudioDeviceInfo audioDeviceInfo1 =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_HDMI).build();
    AudioDeviceInfo audioDeviceInfo2 =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP).build();
    AtomicReference<AudioRouting> listenerRouting = new AtomicReference<>();

    ShadowAudioTrack.setRoutedDevice(audioDeviceInfo1);
    audioTrack.addOnRoutingChangedListener(
        (OnRoutingChangedListener) listenerRouting::set, new Handler(Looper.getMainLooper()));
    ShadowLooper.idleMainLooper();

    assertThat(listenerRouting.get()).isEqualTo(audioTrack);
    assertThat(listenerRouting.get().getRoutedDevice()).isEqualTo(audioDeviceInfo1);

    ShadowAudioTrack.setRoutedDevice(audioDeviceInfo2);
    ShadowLooper.idleMainLooper();

    assertThat(listenerRouting.get()).isEqualTo(audioTrack);
    assertThat(listenerRouting.get().getRoutedDevice()).isEqualTo(audioDeviceInfo2);
  }

  @Test
  @Config(minSdk = N)
  public void setRoutedDevice_toNull_listenerCalled() {
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    AudioDeviceInfo audioDeviceInfo =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_HDMI).build();
    AtomicReference<AudioRouting> listenerRouting = new AtomicReference<>();
    ShadowAudioTrack.setRoutedDevice(audioDeviceInfo);
    audioTrack.addOnRoutingChangedListener(
        (OnRoutingChangedListener) listenerRouting::set, new Handler(Looper.getMainLooper()));
    ShadowLooper.idleMainLooper();

    ShadowAudioTrack.setRoutedDevice(null);
    ShadowLooper.idleMainLooper();

    assertThat(listenerRouting.get()).isEqualTo(audioTrack);
    assertThat(listenerRouting.get().getRoutedDevice()).isEqualTo(null);
  }

  @Test
  @Config(minSdk = N)
  public void removeOnRoutingChangedListener_noFurtherUpdatesSent() {
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    AudioDeviceInfo audioDeviceInfo1 =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_HDMI).build();
    AudioDeviceInfo audioDeviceInfo2 =
        AudioDeviceInfoBuilder.newBuilder().setType(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER).build();
    ShadowAudioTrack.setRoutedDevice(audioDeviceInfo1);
    AtomicInteger listenerCounter1 = new AtomicInteger();
    AtomicInteger listenerCounter2 = new AtomicInteger();
    OnRoutingChangedListener listener1 = routing -> listenerCounter1.incrementAndGet();
    OnRoutingChangedListener listener2 = routing -> listenerCounter2.incrementAndGet();
    audioTrack.addOnRoutingChangedListener(listener1, new Handler(Looper.getMainLooper()));
    audioTrack.addOnRoutingChangedListener(listener2, new Handler(Looper.getMainLooper()));
    ShadowLooper.idleMainLooper();

    audioTrack.removeOnRoutingChangedListener(listener1);
    ShadowAudioTrack.setRoutedDevice(audioDeviceInfo2);
    ShadowLooper.idleMainLooper();

    assertThat(listenerCounter1.get()).isEqualTo(1);
    assertThat(listenerCounter2.get()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = N)
  public void play_illegalStateOnPlayEnabled_throws() {
    ShadowAudioTrack.enableIllegalStateOnPlay(/* enabled= */ true);
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    assertThrows(IllegalStateException.class, audioTrack::play);
  }

  @Test
  @Config(minSdk = N)
  public void play_illegalStateOnPlayEnabled_thenDisabled_notThrowing() {
    ShadowAudioTrack.enableIllegalStateOnPlay(/* enabled= */ true);
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    ShadowAudioTrack.enableIllegalStateOnPlay(/* enabled= */ false);
    audioTrack.play();
  }

  @Test
  @Config(minSdk = N)
  public void play_illegalStateOnPlayEnabled_reset_notThrowing() {
    ShadowAudioTrack.enableIllegalStateOnPlay(/* enabled= */ true);
    AudioTrack audioTrack = new AudioTrack.Builder().build();
    ShadowAudioTrack.resetTest();
    audioTrack.play();
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

  private AudioFormat getAudioFormat(int encoding) {
    return new AudioFormat.Builder().setEncoding(encoding).build();
  }
}

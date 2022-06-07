package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.max;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CodecException;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.verification.VerificationMode;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowMediaCodec.CodecConfig;
import org.robolectric.shadows.ShadowMediaCodec.CodecConfig.Codec;

/** Tests for {@link ShadowMediaCodec}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public final class ShadowMediaCodecTest {
  private static final String AUDIO_MIME = "audio/fake";
  private static final String AUDIO_DECODER_NAME = "audio-fake.decoder";
  private static final String AUDIO_ENCODER_NAME = "audio-fake.encoder";
  private static final int WITHOUT_TIMEOUT = -1;

  private MediaCodec.Callback callback;

  @After
  public void tearDown() throws Exception {
    ShadowMediaCodec.clearCodecs();
  }

  @Test
  public void constructShadowMediaCodec_byDecoderName_succeeds() throws Exception {
    // Add an audio decoder to the MediaCodecList.
    MediaFormat mediaFormat = new MediaFormat();
    mediaFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME);
    ShadowMediaCodecList.addCodec(
        MediaCodecInfoBuilder.newBuilder()
            .setName(AUDIO_DECODER_NAME)
            .setCapabilities(
                MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
                    .setMediaFormat(mediaFormat)
                    .build())
            .build());
    ShadowMediaCodec.addDecoder(
        AUDIO_DECODER_NAME,
        new CodecConfig(/* inputBufferSize= */ 0, /* outputBufferSize= */ 0, (in, out) -> {}));

    MediaCodec codec = MediaCodec.createByCodecName(AUDIO_DECODER_NAME);

    assertThat(codec.getCodecInfo().getName()).isEqualTo(AUDIO_DECODER_NAME);
    assertThat(codec.getCodecInfo().isEncoder()).isFalse();
  }

  @Test
  public void constructShadowMediaCodec_byEncoderName_succeeds() throws Exception {
    // Add an audio encoder to the MediaCodecList.
    MediaFormat mediaFormat = new MediaFormat();
    mediaFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME);
    ShadowMediaCodecList.addCodec(
        MediaCodecInfoBuilder.newBuilder()
            .setName(AUDIO_ENCODER_NAME)
            .setIsEncoder(true)
            .setCapabilities(
                MediaCodecInfoBuilder.CodecCapabilitiesBuilder.newBuilder()
                    .setMediaFormat(mediaFormat)
                    .setIsEncoder(true)
                    .build())
            .build());
    ShadowMediaCodec.addEncoder(
        AUDIO_ENCODER_NAME,
        new CodecConfig(/* inputBufferSize= */ 0, /* outputBufferSize= */ 0, (in, out) -> {}));

    MediaCodec codec = MediaCodec.createByCodecName(AUDIO_ENCODER_NAME);

    assertThat(codec.getCodecInfo().getName()).isEqualTo(AUDIO_ENCODER_NAME);
    assertThat(codec.getCodecInfo().isEncoder()).isTrue();
  }

  @Test
  public void dequeueInputBuffer_inAsyncMode_throws() throws IOException {
    MediaCodec codec = createAsyncEncoder();

    try {
      codec.dequeueInputBuffer(/* timeoutUs= */ 0);
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void dequeueOutputBuffer_inASyncMode_throws() throws IOException {
    MediaCodec codec = createAsyncEncoder();

    try {
      codec.dequeueOutputBuffer(new BufferInfo(), /* timeoutUs= */ 0);
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void dequeueAllInputBuffersThenReleaseOutputBuffer_allowsDequeueInputBuffer()
      throws IOException {
    MediaCodec codec = createSyncEncoder();
    int bufferIndex;
    ByteBuffer buffer;

    for (int i = 0; i < ShadowMediaCodec.BUFFER_COUNT; i++) {
      bufferIndex = codec.dequeueInputBuffer(/* timeoutUs= */ 0);
      assertThat(bufferIndex).isNotEqualTo(MediaCodec.INFO_TRY_AGAIN_LATER);
      buffer = codec.getInputBuffer(bufferIndex);

      int start = buffer.position();
      // "Write" to the buffer.
      buffer.position(buffer.limit());
      codec.queueInputBuffer(
          bufferIndex,
          /* offset= */ start,
          /* size= */ buffer.position() - start,
          /* presentationTimeUs= */ 0,
          /* flags= */ 0);
    }

    // Cannot dequeue buffer after all available buffers are dequeued.
    bufferIndex = codec.dequeueInputBuffer(/* timeoutUs= */ 0);
    assertThat(bufferIndex).isEqualTo(MediaCodec.INFO_TRY_AGAIN_LATER);

    // The first dequeueOutputBuffer should return MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
    codec.dequeueOutputBuffer(new BufferInfo(), /* timeoutUs= */ 0);
    bufferIndex = codec.dequeueOutputBuffer(new BufferInfo(), /* timeoutUs= */ 0);

    codec.releaseOutputBuffer(bufferIndex, /* render= */ false);
    // We should be able to dequeue the corresponding input buffer.
    int dequeuedInputbufferIndex = codec.dequeueInputBuffer(/* timeoutUs= */ 0);
    assertThat(dequeuedInputbufferIndex).isEqualTo(bufferIndex);
  }

  @Test
  public void getInputBuffer_withInvalidIndex_returnsNull() throws IOException {
    List<Integer> inputBuffers = new ArrayList<>();
    MediaCodecCallback callback =
        new MediaCodecCallback() {
          @Override
          public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {
            inputBuffers.add(inputBufferId);
          }
        };
    MediaCodec codec = createAsyncEncoder(callback);
    int invalidInputIndex = inputBuffers.isEmpty() ? 0 : max(inputBuffers) + 1;

    assertThat(codec.getInputBuffer(invalidInputIndex)).isNull();
  }

  @Test
  public void queueInputBuffer_withInvalidIndex_throws() throws IOException {
    List<Integer> inputBuffers = new ArrayList<>();
    MediaCodecCallback callback =
        new MediaCodecCallback() {
          @Override
          public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {
            inputBuffers.add(inputBufferId);
          }
        };

    MediaCodec codec = createAsyncEncoder(callback);
    int invalidInputIndex = inputBuffers.isEmpty() ? 0 : max(inputBuffers) + 1;

    try {
      codec.queueInputBuffer(
          invalidInputIndex,
          /* offset= */ 0,
          /* size= */ 128,
          /* presentationTimeUs= */ 0,
          /* flags= */ 0);
      fail();
    } catch (CodecException expected) {
    }
  }

  @Test
  public void getOutputBuffer_withInvalidIndex_returnsNull() throws IOException {
    MediaCodec codec = createAsyncEncoder();

    assertThat(codec.getOutputBuffer(/* index= */ -1)).isNull();
  }

  @Test
  public void formatChangeReported() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    verify(callback).onOutputFormatChanged(same(codec), any());
  }

  @Test
  public void presentsInputBuffer() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    verify(callback).onInputBufferAvailable(same(codec), anyInt());
  }

  @Test
  public void providesValidInputBuffer() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(callback).onInputBufferAvailable(same(codec), indexCaptor.capture());

    ByteBuffer buffer = codec.getInputBuffer(indexCaptor.getValue());

    assertThat(buffer.remaining()).isGreaterThan(0);
  }

  @Test
  public void presentsOutputBufferAfterQueuingInputBuffer() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(callback).onInputBufferAvailable(same(codec), indexCaptor.capture());

    ByteBuffer buffer = codec.getInputBuffer(indexCaptor.getValue());

    int start = buffer.position();
    // "Write" to the buffer.
    buffer.position(buffer.limit());

    codec.queueInputBuffer(
        indexCaptor.getValue(),
        /* offset= */ start,
        /* size= */ buffer.position() - start,
        /* presentationTimeUs= */ 0,
        /* flags= */ 0);

    asyncVerify(callback).onOutputBufferAvailable(same(codec), anyInt(), any());
  }

  @Test
  public void providesValidOutputBuffer() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(callback).onInputBufferAvailable(same(codec), indexCaptor.capture());

    ByteBuffer buffer = codec.getInputBuffer(indexCaptor.getValue());

    int start = buffer.position();
    // "Write" to the buffer.
    buffer.position(buffer.limit());

    codec.queueInputBuffer(
        indexCaptor.getValue(),
        /* offset= */ start,
        /* size= */ buffer.position() - start,
        /* presentationTimeUs= */ 0,
        /* flags= */ 0);

    asyncVerify(callback).onOutputBufferAvailable(same(codec), indexCaptor.capture(), any());

    buffer = codec.getOutputBuffer(indexCaptor.getValue());

    assertThat(buffer.remaining()).isGreaterThan(0);
  }

  @Test
  public void presentsInputBufferAfterReleasingOutputBufferWhenNotFinished() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(callback).onInputBufferAvailable(same(codec), indexCaptor.capture());

    ByteBuffer buffer = codec.getInputBuffer(indexCaptor.getValue());

    int start = buffer.position();
    // "Write" to the buffer.
    buffer.position(buffer.limit());

    codec.queueInputBuffer(
        indexCaptor.getValue(),
        /* offset= */ start,
        /* size= */ buffer.position() - start,
        /* presentationTimeUs= */ 0,
        /* flags= */ 0);

    asyncVerify(callback).onOutputBufferAvailable(same(codec), indexCaptor.capture(), any());

    codec.releaseOutputBuffer(indexCaptor.getValue(), /* render= */ false);

    asyncVerify(callback, times(2)).onInputBufferAvailable(same(codec), anyInt());
  }

  @Test
  public void doesNotPresentInputBufferAfterReleasingOutputBufferFinished() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(callback).onInputBufferAvailable(same(codec), indexCaptor.capture());

    ByteBuffer buffer = codec.getInputBuffer(indexCaptor.getValue());

    int start = buffer.position();
    // "Write" to the buffer.
    buffer.position(buffer.limit());

    codec.queueInputBuffer(
        indexCaptor.getValue(),
        /* offset= */ start,
        /* size= */ buffer.position() - start,
        /* presentationTimeUs= */ 0,
        /* flags= */ MediaCodec.BUFFER_FLAG_END_OF_STREAM);

    asyncVerify(callback).onOutputBufferAvailable(same(codec), indexCaptor.capture(), any());

    codec.releaseOutputBuffer(indexCaptor.getValue(), /* render= */ false);

    asyncVerify(callback, times(2)).onInputBufferAvailable(same(codec), anyInt());
  }

  @Test
  public void passesEndOfStreamFlagWithFinalOutputBuffer() throws IOException {
    MediaCodec codec = createAsyncEncoder();
    ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(callback).onInputBufferAvailable(same(codec), indexCaptor.capture());

    ByteBuffer buffer = codec.getInputBuffer(indexCaptor.getValue());

    int start = buffer.position();
    // "Write" to the buffer.
    buffer.position(buffer.limit());

    codec.queueInputBuffer(
        indexCaptor.getValue(),
        /* offset= */ start,
        /* size= */ buffer.position() - start,
        /* presentationTimeUs= */ 0,
        /* flags= */ MediaCodec.BUFFER_FLAG_END_OF_STREAM);

    ArgumentCaptor<BufferInfo> infoCaptor = ArgumentCaptor.forClass(BufferInfo.class);

    asyncVerify(callback)
        .onOutputBufferAvailable(same(codec), indexCaptor.capture(), infoCaptor.capture());

    assertThat(infoCaptor.getValue().flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM).isNotEqualTo(0);
  }

  @Test
  public void whenCustomCodec_InputBufferIsOfExpectedSize() throws Exception {
    int inputBufferSize = 1000;
    CodecConfig config = new CodecConfig(inputBufferSize, /*outputBufferSize=*/ 0, (in, out) -> {});
    ShadowMediaCodec.addEncoder(AUDIO_MIME, config);

    MediaCodec codec = createSyncEncoder();

    ByteBuffer inputBuffer = codec.getInputBuffer(codec.dequeueInputBuffer(0));
    assertThat(inputBuffer.capacity()).isEqualTo(inputBufferSize);
  }

  @Test
  public void whenCustomCodec_OutputBufferIsOfExpectedSize() throws Exception {
    int outputBufferSize = 1000;
    CodecConfig config = new CodecConfig(/*inputBufferSize=*/ 0, outputBufferSize, (in, out) -> {});
    ShadowMediaCodec.addEncoder(AUDIO_MIME, config);
    MediaCodec codec = createSyncEncoder();

    int inputBuffer = codec.dequeueInputBuffer(/*timeoutUs=*/ 0);
    codec.queueInputBuffer(
        inputBuffer, /* offset=*/ 0, /* size=*/ 0, /* presentationTimeUs=*/ 0, /* flags=*/ 0);

    assertThat(codec.dequeueOutputBuffer(new BufferInfo(), /* timeoutUs= */ 0))
        .isEqualTo(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED);

    ByteBuffer outputBuffer =
        codec.getOutputBuffer(codec.dequeueOutputBuffer(new BufferInfo(), /*timeoutUs=*/ 0));
    assertThat(outputBuffer.capacity()).isEqualTo(outputBufferSize);
  }

  @Test
  public void customDecoder_setsCorrectOutputFormat() throws IOException {
    Codec mockDecoder = mock(Codec.class);
    ShadowMediaCodec.addDecoder(
        AUDIO_MIME,
        new ShadowMediaCodec.CodecConfig(
            /* inputBufferSize= */ 10, /* outputBufferSize= */ 10, mockDecoder));

    // Creates decoder and configures with AAC format.
    MediaFormat outputFormat = getBasicAacFormat();
    MediaCodec codec = MediaCodec.createDecoderByType(AUDIO_MIME);
    codec.configure(outputFormat, /* surface= */ null, /* crypto= */ null, /* flags= */ 0);
    codec.start();

    ArgumentCaptor<MediaFormat> mediaFormatCaptor = ArgumentCaptor.forClass(MediaFormat.class);
    verify(mockDecoder)
        .onConfigured(
            mediaFormatCaptor.capture(),
            nullable(Surface.class),
            nullable(MediaCrypto.class),
            anyInt());
    MediaFormat capturedFormat = mediaFormatCaptor.getValue();

    assertThat(capturedFormat.getString(MediaFormat.KEY_MIME))
        .isEqualTo(outputFormat.getString(MediaFormat.KEY_MIME));
    assertThat(capturedFormat.getInteger(MediaFormat.KEY_BIT_RATE))
        .isEqualTo(outputFormat.getInteger(MediaFormat.KEY_BIT_RATE));
    assertThat(capturedFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT))
        .isEqualTo(outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
    assertThat(capturedFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE))
        .isEqualTo(outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
    assertThat(capturedFormat.getInteger(MediaFormat.KEY_AAC_PROFILE))
        .isEqualTo(outputFormat.getInteger(MediaFormat.KEY_AAC_PROFILE));
  }

  @Test
  public void inSyncMode_outputBufferInfoPopulated() throws Exception {
    MediaCodec codec = createSyncEncoder();
    int inputBuffer = codec.dequeueInputBuffer(/*timeoutUs=*/ 0);
    codec.getInputBuffer(inputBuffer).put(ByteBuffer.allocateDirect(512));
    codec.queueInputBuffer(
        inputBuffer,
        /* offset= */ 0,
        /* size= */ 512,
        /* presentationTimeUs= */ 123456,
        /* flags= */ MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    BufferInfo info = new BufferInfo();

    assertThat(codec.dequeueOutputBuffer(info, /* timeoutUs= */ 0))
        .isEqualTo(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED);

    codec.dequeueOutputBuffer(info, /* timeoutUs= */ 0);

    assertThat(info.offset).isEqualTo(0);
    assertThat(info.size).isEqualTo(512);
    assertThat(info.flags).isEqualTo(MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    assertThat(info.presentationTimeUs).isEqualTo(123456);
  }

  @Test
  public void inSyncMode_encodedDataIsCorrect() throws Exception {
    ByteBuffer src = ByteBuffer.wrap(generateByteArray(512));
    ByteBuffer dst = ByteBuffer.wrap(new byte[512]);

    MediaCodec codec = createSyncEncoder();
    process(codec, src, dst);

    src.clear();
    dst.clear();
    assertThat(dst.array()).isEqualTo(generateByteArray(512));
  }

  @Test
  public void inSyncMode_encodedDataIsCorrectForCustomCodec() throws Exception {
    ShadowMediaCodec.addEncoder(
        AUDIO_MIME,
        new CodecConfig(
            1000,
            100,
            (in, out) -> {
              ByteBuffer inClone = in.duplicate();
              inClone.limit(in.remaining() / 10);
              out.put(inClone);
            }));
    byte[] input = generateByteArray(4000);
    ByteBuffer src = ByteBuffer.wrap(input);
    ByteBuffer dst = ByteBuffer.wrap(new byte[400]);

    MediaCodec codec = createSyncEncoder();
    process(codec, src, dst);

    assertThat(Arrays.copyOf(dst.array(), 100)).isEqualTo(copyOfRange(input, 0, 100));
    assertThat(copyOfRange(dst.array(), 100, 200)).isEqualTo(copyOfRange(input, 1000, 1100));
    assertThat(copyOfRange(dst.array(), 200, 300)).isEqualTo(copyOfRange(input, 2000, 2100));
    assertThat(copyOfRange(dst.array(), 300, 400)).isEqualTo(copyOfRange(input, 3000, 3100));
  }

  @Test
  public void inSyncMode_codecInitiallyOutputsConfiguredFormat() throws Exception {
    MediaCodec codec = createSyncEncoder();
    assertThat(codec.dequeueOutputBuffer(new BufferInfo(), /* timeoutUs= */ 0))
        .isEqualTo(MediaCodec.INFO_OUTPUT_FORMAT_CHANGED);

    MediaFormat codecFormat = codec.getOutputFormat();
    MediaFormat basicAacFormat = getBasicAacFormat();
    assertThat(codecFormat.getString(MediaFormat.KEY_MIME))
        .isEqualTo(basicAacFormat.getString(MediaFormat.KEY_MIME));
    assertThat(codecFormat.getInteger(MediaFormat.KEY_BIT_RATE))
        .isEqualTo(basicAacFormat.getInteger(MediaFormat.KEY_BIT_RATE));
    assertThat(codecFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT))
        .isEqualTo(basicAacFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
    assertThat(codecFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE))
        .isEqualTo(basicAacFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
    assertThat(codecFormat.getInteger(MediaFormat.KEY_AAC_PROFILE))
        .isEqualTo(basicAacFormat.getInteger(MediaFormat.KEY_AAC_PROFILE));
  }

  @Test
  public void inSyncMode_getInputBufferWithoutDequeue_returnsNull() throws IOException {
    MediaCodec codec = createSyncEncoder();

    assertThat(codec.getInputBuffer(/* index= */ 0)).isNull();
  }

  @Test
  public void inSyncMode_queueingInputBufferWithoutDequeue_throws() throws IOException {
    MediaCodec codec = createSyncEncoder();

    try {
      codec.queueInputBuffer(
          /* index= */ 0,
          /* offset= */ 0,
          /* size= */ 0,
          /* presentationTimeUs= */ 0,
          /* flags= */ 0);
      fail();
    } catch (CodecException expected) {
    }
  }

  @Test
  public void inSyncMode_queueInputBufferTwice_throws() throws IOException {
    MediaCodec codec = createSyncEncoder();

    int inputIndex = codec.dequeueInputBuffer(/* timeoutUs= */ 0);
    codec.getInputBuffer(inputIndex).put(generateByteArray(/* size= */ 128));
    codec.queueInputBuffer(
        inputIndex, /* offset= */ 0, /* size= */ 128, /* presentationTimeUs= */ 0, /* flags= */ 0);

    try {
      codec.queueInputBuffer(
          inputIndex,
          /* offset= */ 0,
          /* size= */ 128,
          /* presentationTimeUs= */ 0,
          /* flags= */ 0);
      fail();
    } catch (CodecException expected) {
    }
  }

  @Test
  public void inSyncMode_flushDiscardsQueuedInputBuffer() throws IOException {
    MediaCodec codec = createSyncEncoder();
    // Dequeue the output format
    codec.dequeueOutputBuffer(new BufferInfo(), /* timeoutUs= */ 0);

    int inputBufferIndex = codec.dequeueInputBuffer(/* timeoutUs= */ 0);
    codec.getInputBuffer(inputBufferIndex).put(generateByteArray(/* size= */ 128));
    codec.queueInputBuffer(
        inputBufferIndex,
        /* offset= */ 0,
        /* size= */ 128,
        /* presentationTimeUs= */ 123456,
        /* flags= */ 0);
    codec.flush();

    assertThat(codec.dequeueOutputBuffer(new BufferInfo(), /* timeoutUs= */ 0))
        .isEqualTo(MediaCodec.INFO_TRY_AGAIN_LATER);
    assertThat(codec.dequeueInputBuffer(/* timeoutUs= */ 0)).isEqualTo(inputBufferIndex);
    assertThat(codec.getInputBuffer(inputBufferIndex).position()).isEqualTo(0);
  }

  @Test
  public void inSyncMode_flushProvidesInputBuffersAgain() throws IOException {
    MediaCodec codec = createSyncEncoder();

    // Dequeue all input buffers
    while (codec.dequeueInputBuffer(/* timeoutUs= */ 0) != MediaCodec.INFO_TRY_AGAIN_LATER) {}
    codec.flush();

    assertThat(codec.dequeueInputBuffer(/* timeoutUs= */ 0)).isAtLeast(0);
  }

  @Test
  public void inSyncMode_afterFlushGetInputBuffer_returnsNull() throws IOException {
    MediaCodec codec = createSyncEncoder();

    int inputIndex = codec.dequeueInputBuffer(/* timeoutUs= */ 0);
    codec.flush();

    assertThat(codec.getInputBuffer(inputIndex)).isNull();
  }

  @Test
  public void inSyncMode_afterFlushCannotQueueInputBufferThatIsNotDequeued() throws IOException {
    MediaCodec codec = createSyncEncoder();

    int inputIndex = codec.dequeueInputBuffer(/* timeoutUs= */ 0);
    codec.getInputBuffer(inputIndex).put(generateByteArray(/* size= */ 128));
    codec.flush();

    try {
      codec.queueInputBuffer(
          inputIndex,
          /* offset= */ 0,
          /* size= */ 128,
          /* presentationTimeUs= */ 0,
          /* flags= */ 0);
      fail();
    } catch (CodecException expected) {
    }
  }

  public static <T> T asyncVerify(T mock) {
    shadowMainLooper().idle();
    return verify(mock);
  }

  public static <T> T asyncVerify(T mock, VerificationMode mode) {
    shadowMainLooper().idle();
    return verify(mock, mode);
  }

  private MediaCodec createAsyncEncoder() throws IOException {
    callback = mock(MediaCodecCallback.class);
    return createAsyncEncoder(callback);
  }

  private static MediaCodec createAsyncEncoder(MediaCodec.Callback callback) throws IOException {
    MediaCodec codec = MediaCodec.createEncoderByType(AUDIO_MIME);
    codec.setCallback(callback);

    codec.configure(
        getBasicAacFormat(),
        /* surface= */ null,
        /* crypto= */ null,
        MediaCodec.CONFIGURE_FLAG_ENCODE);
    codec.start();

    shadowMainLooper().idle();

    return codec;
  }

  private static MediaCodec createSyncEncoder() throws IOException {
    MediaCodec codec = MediaCodec.createEncoderByType(AUDIO_MIME);
    codec.configure(
        getBasicAacFormat(),
        /* surface= */ null,
        /* crypto= */ null,
        MediaCodec.CONFIGURE_FLAG_ENCODE);
    codec.start();

    shadowMainLooper().idle();

    return codec;
  }

  private static MediaFormat getBasicAacFormat() {
    MediaFormat format = new MediaFormat();
    format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC);
    format.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
    format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
    format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 16000);
    format.setInteger(MediaFormat.KEY_AAC_PROFILE, CodecProfileLevel.AACObjectLC);

    return format;
  }

  /** Concrete class extending MediaCodec.Callback to facilitate mocking. */
  public static class MediaCodecCallback extends MediaCodec.Callback {

    @Override
    public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {}

    @Override
    public void onOutputBufferAvailable(MediaCodec codec, int outputBufferId, BufferInfo info) {}

    @Override
    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {}

    @Override
    public void onError(MediaCodec codec, MediaCodec.CodecException e) {}
  }

  /**
   * A pure function which generates a byte[] of a given size contain values between {@link
   * Byte#MIN_VALUE} and {@link Byte#MAX_VALUE},
   */
  private static byte[] generateByteArray(int size) {
    byte[] array = new byte[size];
    for (int i = 0; i < size; i++) {
      array[i] = (byte) (i % 255 - Byte.MIN_VALUE);
    }
    return array;
  }

  /**
   * Simply moves the data in the {@code src} buffer across a given {@link MediaCodec} and stores
   * the output in {@code dst}.
   */
  private static void process(MediaCodec codec, ByteBuffer src, ByteBuffer dst) {
    while (true) {
      if (src.hasRemaining()) {
        writeToInputBuffer(codec, src);
        if (!src.hasRemaining()) {
          writeEndOfInput(codec);
        }
      }

      BufferInfo info = new BufferInfo();
      int outputBufferId = codec.dequeueOutputBuffer(info, 0);
      if (outputBufferId >= 0) {
        ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
        dst.put(outputBuffer);
        codec.releaseOutputBuffer(outputBufferId, false);
      }

      if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
        break;
      }
    }
    codec.stop();
    codec.release();
  }

  /** Writes as much of {@code src} to the next available input buffer. */
  private static void writeToInputBuffer(MediaCodec codec, ByteBuffer src) {
    int inputBufferId = codec.dequeueInputBuffer(WITHOUT_TIMEOUT);
    ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
    // API versions lower than 21 don't clear the buffer before returning it.
    if (Build.VERSION.SDK_INT < 21) {
      inputBuffer.clear();
    }
    int srcLimit = src.limit();
    int numberOfBytesToWrite = Math.min(src.remaining(), inputBuffer.remaining());
    src.limit(src.position() + numberOfBytesToWrite);
    inputBuffer.put(src);
    src.limit(srcLimit);
    codec.queueInputBuffer(inputBufferId, 0, numberOfBytesToWrite, 0, 0);
  }

  /** Writes end of input to the next available input buffer */
  private static void writeEndOfInput(MediaCodec codec) {
    int inputBufferId = codec.dequeueInputBuffer(WITHOUT_TIMEOUT);
    codec.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
  }
}

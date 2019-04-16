package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.Callback;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaFormat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.verification.VerificationMode;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowMediaCodec}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowMediaCodecTest {
  private MediaCodec codec;
  private Callback callback;

  @Before
  public void startMediaCodec() throws IOException {
    MediaFormat format = getBasicAACFormat();

    callback = mock(MediaCodecCallback.class);
    codec = MediaCodec.createByCodecName("fakePassthroughCodec");
    codec.setCallback(callback);

    codec.configure(
        format, /* surface= */ null, /* crypto= */ null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    codec.start();
    shadowMainLooper().idle();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void formatChangeReported() {
    verify(callback).onOutputFormatChanged(same(codec), any());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void presentsInputBuffer() {
    verify(callback).onInputBufferAvailable(same(codec), anyInt());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void providesValidInputBuffer() {
    ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(callback).onInputBufferAvailable(same(codec), indexCaptor.capture());

    ByteBuffer buffer = codec.getInputBuffer(indexCaptor.getValue());

    assertThat(buffer.remaining()).isGreaterThan(0);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void presentsOutputBufferAfterQueuingInputBuffer() {
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
  @Config(minSdk = LOLLIPOP)
  public void providesValidOutputBuffer() {
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
  @Config(minSdk = LOLLIPOP)
  public void presentsInputBufferAfterReleasingOutputBufferWhenNotFinished() {
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
  @Config(minSdk = LOLLIPOP)
  public void doesNotPresentInputBufferAfterReleasingOutputBufferFinished() {
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

    asyncVerify(callback, times(1)).onInputBufferAvailable(same(codec), anyInt());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void passesEndOfStreamFlagWithFinalOutputBuffer() {
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

  public static <T> T asyncVerify(T mock) {
    shadowMainLooper().idle();
    return verify(mock);
  }

  public static <T> T asyncVerify(T mock, VerificationMode mode) {
    shadowMainLooper().idle();
    return verify(mock, mode);
  }

  private static MediaFormat getBasicAACFormat() {
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
}

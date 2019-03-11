package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import java.nio.ByteBuffer;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/**
 * Implementation of {@link android.media.MediaCodec} which only supports a passthrough asynchronous
 * encoding pipeline.
 *
 * <p>Once the pipeline is started, a format change will be reported, switching to an empty
 * {@link android.media.MediaFormat} with fake codec-specific info. Following this, the
 * implementation will present an input buffer, which will be directly copied to an output buffer
 * once queued, which will be subsequently presented to the callback handler.
 */
@Implements(value = MediaCodec.class, minSdk = JELLY_BEAN)
public class ShadowMediaCodec {
  private static final int BUFFER_SIZE = 512;

  // Must keep in sync with MediaCodec.java
  private static final int EVENT_CALLBACK = 1;
  private static final int CB_INPUT_AVAILABLE = 1;
  private static final int CB_OUTPUT_AVAILABLE = 2;
  private static final int CB_OUTPUT_FORMAT_CHANGE = 4;

  @RealObject private MediaCodec realCodec;
  private MediaCodec.Callback callback;

  private final ByteBuffer[] inputBuffers =
      new ByteBuffer[] {ByteBuffer.wrap(new byte[BUFFER_SIZE])};
  private final ByteBuffer[] outputBuffers =
      new ByteBuffer[] {ByteBuffer.wrap(new byte[BUFFER_SIZE])};

  private boolean reachedEos = false;

  // Member methods.

  /** Saves the callback to allow use inside the shadow. */
  @Implementation(minSdk = LOLLIPOP)
  protected void native_setCallback(MediaCodec.Callback callback) {
    this.callback = callback;
  }

  /**
   * Starts the async encoding process, by first reporting a format change event, and then
   * presenting an input buffer to the callback.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void native_start() {
    // Currently only support async-mode.
    if (callback == null) {
      return;
    }

    // Report the format as changed, to simulate adding codec specific info before making input
    // buffers available.
    HashMap<String, Object> format = new HashMap<>();
    format.put("csd-0", ByteBuffer.wrap(new byte[]{0x13, 0x10}));
    format.put("csd-1", ByteBuffer.wrap(new byte[0]));
    postFakeNativeEvent(EVENT_CALLBACK, CB_OUTPUT_FORMAT_CHANGE, 0, format);

    // Reset state.
    reachedEos = false;

    makeInputBufferAvailable(0);
  }

  /** Returns the shadow buffers used for input or output. */
  @Implementation
  protected ByteBuffer[] getBuffers(boolean input) {
    return input ? inputBuffers : outputBuffers;
  }

  /** Returns the input or output buffer corresponding to the given index, or null if invalid. */
  @Implementation(minSdk = LOLLIPOP)
  protected ByteBuffer getBuffer(boolean input, int index) {
    ByteBuffer[] buffers = input ? inputBuffers : outputBuffers;

    return (index >= 0 && index < buffers.length) ? buffers[index] : null;
  }

  /**
   * Triggers presentation of the corresponding output buffer for the given input buffer, and passes
   * the given metadata as buffer info.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void native_queueInputBuffer(
      int index, int offset, int size, long presentationTimeUs, int flags) {
    // Check if this should be the last buffer cycle.
    if ((flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
      reachedEos = true;
    }

    BufferInfo info = new BufferInfo();
    info.set(offset, size, presentationTimeUs, flags);

    makeOutputBufferAvailable(index, info);
  }

  /**
   * Triggers presentation of the corresponding input buffer for the given output buffer, if end of
   * stream has not yet been signaled.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void releaseOutputBuffer(int index, boolean render, boolean updatePTS, long timeNs) {
    if (reachedEos) {
      return;
    }

    makeInputBufferAvailable(index);
  }

  private void makeInputBufferAvailable(int index) {
    if (index < 0 || index >= inputBuffers.length) {
      throw new IndexOutOfBoundsException("Cannot make non-existent input available.");
    }

    // Reset the input buffer.
    inputBuffers[index].clear();

    // Signal input buffer availability.
    postFakeNativeEvent(EVENT_CALLBACK, CB_INPUT_AVAILABLE, index, null);
  }

  private void makeOutputBufferAvailable(int index, BufferInfo info) {
    if (index < 0 || index >= outputBuffers.length) {
      throw new IndexOutOfBoundsException("Cannot make non-existent output buffer available.");
    }

    // Reset the output buffer.
    outputBuffers[index].clear();

    // Straight copy input -> output.
    inputBuffers[index].rewind();
    outputBuffers[index].put(inputBuffers[index]);
    outputBuffers[index].rewind();

    // Signal output buffer availability.
    postFakeNativeEvent(EVENT_CALLBACK, CB_OUTPUT_AVAILABLE, index, info);
  }

  private void postFakeNativeEvent(int what, int arg1, int arg2, @Nullable Object obj) {
    ReflectionHelpers.callInstanceMethod(
        MediaCodec.class,
        realCodec,
        "postEventFromNative",
        ReflectionHelpers.ClassParameter.from(int.class, what),
        ReflectionHelpers.ClassParameter.from(int.class, arg1),
        ReflectionHelpers.ClassParameter.from(int.class, arg2),
        ReflectionHelpers.ClassParameter.from(Object.class, obj));
  }

  /** Prevents calling Android-only methods on basic ByteBuffer objects. */
  @Implementation(minSdk = LOLLIPOP)
  protected void invalidateByteBuffer(@Nullable ByteBuffer[] buffers, int index) {}

  /** Prevents calling Android-only methods on basic ByteBuffer objects. */
  @Implementation(minSdk = LOLLIPOP)
  protected void validateInputByteBuffer(@Nullable ByteBuffer[] buffers, int index) {}

  /** Prevents calling Android-only methods on basic ByteBuffer objects. */
  @Implementation(minSdk = LOLLIPOP)
  protected void revalidateByteBuffer(@Nullable ByteBuffer[] buffers, int index) {}

  /**
   * Prevents calling Android-only methods on basic ByteBuffer objects. Replicates existing behavior
   * adjusting buffer positions and limits.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void validateOutputByteBuffer(
      @Nullable ByteBuffer[] buffers, int index, @NonNull BufferInfo info) {
    if (buffers != null && index >= 0 && index < buffers.length) {
      ByteBuffer buffer = buffers[index];
      if (buffer != null) {
        buffer.limit(info.offset + info.size).position(info.offset);
      }
    }
  }

  /** Prevents calling Android-only methods on basic ByteBuffer objects. */
  @Implementation(minSdk = LOLLIPOP)
  protected void invalidateByteBuffers(@Nullable ByteBuffer[] buffers) {}

  /** Prevents attempting to free non-direct ByteBuffer objects. */
  @Implementation(minSdk = LOLLIPOP)
  protected void freeByteBuffer(@Nullable ByteBuffer buffer) {}

  /** Shadows CodecBuffer to prevent attempting to free non-direct ByteBuffer objects. */
  @Implements(className = "android.media.MediaCodec$BufferMap$CodecBuffer", minSdk = LOLLIPOP)
  protected static class ShadowCodecBuffer {

    // Seems to be required to work.
    public ShadowCodecBuffer() {}

    // Seems to be required to work.
    @Implementation
    protected void __constructor__() {}

    /** Prevents attempting to free non-direct ByteBuffer objects. */
    @Implementation
    protected void free() {}
  }
}

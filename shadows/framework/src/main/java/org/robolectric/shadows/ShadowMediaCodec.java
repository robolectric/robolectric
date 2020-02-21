package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.view.Surface;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Implementation of {@link android.media.MediaCodec} which supports both asynchronous and
 * synchronous modes.
 *
 * <p>By default for any encoded required, a 1 to 1 mapping will be used between the input and
 * output buffers. Data from a queued input buffer will be copied to the output buffer. In the case
 * that is it necessary so simulate some form of data compression, a custom encoder or decoder can
 * be added via {@link #addEncoder(String, CodecConfig)} and {@link #addDecoder(String,
 * CodecConfig)} respectively.
 *
 * <p>Asynchronous mode: Once the codec is started, a format change will be reported, switching to
 * an empty {@link android.media.MediaFormat} with fake codec-specific info. Following this, the
 * implementation will present an input buffer, which will be copied to an output buffer once
 * queued, which will be subsequently presented to the callback handler.
 */
@Implements(value = MediaCodec.class, minSdk = JELLY_BEAN, looseSignatures = true)
public class ShadowMediaCodec {
  private static final int DEFAULT_BUFFER_SIZE = 512;
  private static final int BUFFER_COUNT = 10;

  // Must keep in sync with MediaCodec.java
  private static final int EVENT_CALLBACK = 1;
  private static final int CB_INPUT_AVAILABLE = 1;
  private static final int CB_OUTPUT_AVAILABLE = 2;
  private static final int CB_OUTPUT_FORMAT_CHANGE = 4;

  private static final Map<String, CodecConfig> encoders = new HashMap<>();
  private static final Map<String, CodecConfig> decoders = new HashMap<>();
  private static MediaFormat outputFormat;

  /**
   * Default codec that simply moves bytes from the input to the output buffers where the buffers
   * are of equal size.
   */
  private static final CodecConfig DEFAULT_CODEC =
      new CodecConfig(DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE, (in, out) -> out.put(in));

  /** Add a fake encoding codec to the Shadow. */
  public static void addEncoder(String type, CodecConfig config) {
    encoders.put(type, config);
  }

  /** Add a fake decoding codec to the Shadow. */
  public static void addDecoder(String type, CodecConfig config) {
    decoders.put(type, config);
  }

  /** Clears any previously added encoders and decoders. */
  public static void clearCodecs() {
    encoders.clear();
    decoders.clear();
  }

  @RealObject private MediaCodec realCodec;
  @Nullable private CodecConfig.Codec fakeCodec;

  @Nullable private MediaCodec.Callback callback;

  @Nullable private MediaFormat pendingOutputFormat;

  private final BlockingQueue<Integer> inputBufferAvailableIndexes = new LinkedBlockingDeque<>();
  private final BlockingQueue<Integer> outputBufferAvailableIndexes = new LinkedBlockingDeque<>();

  private final ByteBuffer[] inputBuffers = new ByteBuffer[BUFFER_COUNT];
  private final ByteBuffer[] outputBuffers = new ByteBuffer[BUFFER_COUNT];
  private final BufferInfo[] outputBufferInfos = new BufferInfo[BUFFER_COUNT];

  private boolean isAsync = false;
  private boolean reachedEos = false;

  // Member methods.

  @Implementation
  protected void __constructor__(String name, boolean nameIsType, boolean encoder) {
    invokeConstructor(
        MediaCodec.class,
        realCodec,
        ClassParameter.from(String.class, name),
        ClassParameter.from(boolean.class, nameIsType),
        ClassParameter.from(boolean.class, encoder));

    CodecConfig codecConfig =
        encoder
            ? encoders.getOrDefault(name, DEFAULT_CODEC)
            : decoders.getOrDefault(name, DEFAULT_CODEC);
    fakeCodec = codecConfig.codec;

    for (int i = 0; i < BUFFER_COUNT; i++) {
      inputBuffers[i] = ByteBuffer.allocateDirect(codecConfig.inputBufferSize);
      outputBuffers[i] = ByteBuffer.allocateDirect(codecConfig.outputBufferSize);
      outputBufferInfos[i] = new BufferInfo();
      inputBufferAvailableIndexes.add(i);
    }
  }

  /** Saves the callback to allow use inside the shadow. */
  @Implementation(minSdk = LOLLIPOP)
  protected void native_setCallback(MediaCodec.Callback callback) {
    this.callback = callback;
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected void native_configure(
      String[] keys, Object[] values, Surface surface, MediaCrypto crypto, int flags) {
    configure(keys, values);
  }

  @Implementation(minSdk = O)
  protected void native_configure(
      Object keys,
      Object values,
      Object surface,
      Object crypto,
      Object descramblerBinder,
      Object flags) {
    configure((String[]) keys, (Object[]) values);
  }

  private void configure(String[] keys, Object[] values) {
    isAsync = callback != null;
    pendingOutputFormat = recreateMediaFormatFromKeysValues(keys, values);
  }

  /**
   * Starts the async encoding process, by first reporting a format change event, and then
   * presenting an input buffer to the callback.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void native_start() {
    // Reset state
    reachedEos = false;
    inputBufferAvailableIndexes.clear();
    outputBufferAvailableIndexes.clear();
    for (int i = 0; i < BUFFER_COUNT; i++) {
      inputBufferAvailableIndexes.add(i);
    }

    if (isAsync) {
      // Report the format as changed, to simulate adding codec specific info before making input
      // buffers available.
      HashMap<String, Object> format = new HashMap<>();
      format.put("csd-0", ByteBuffer.wrap(new byte[] {0x13, 0x10}));
      format.put("csd-1", ByteBuffer.wrap(new byte[0]));
      postFakeNativeEvent(EVENT_CALLBACK, CB_OUTPUT_FORMAT_CHANGE, 0, format);

      try {
        makeInputBufferAvailable(inputBufferAvailableIndexes.take());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
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

  protected int native_dequeueInputBuffer(long timeoutUs) {
    checkState(!isAsync, "Attempting to deque buffer in Async mode.");
    try {
      Integer index;
      if (timeoutUs < 0) {
        index = inputBufferAvailableIndexes.take();
      } else {
        index = inputBufferAvailableIndexes.poll(timeoutUs, TimeUnit.MICROSECONDS);
      }

      if (index == null) {
        return -1;
      }

      return index;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return -1;
    }
  }

  /**
   * Triggers presentation of the corresponding output buffer for the given input buffer, and passes
   * the given metadata as buffer info.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void native_queueInputBuffer(
      int index, int offset, int size, long presentationTimeUs, int flags) {
    if ((flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
      reachedEos = true;
    }

    BufferInfo info = new BufferInfo();
    info.set(offset, size, presentationTimeUs, flags);

    makeOutputBufferAvailable(index, info);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int native_dequeueOutputBuffer(BufferInfo info, long timeoutUs) {
    try {
      if (pendingOutputFormat != null) {
        outputFormat = pendingOutputFormat;
        pendingOutputFormat = null;
        return MediaCodec.INFO_OUTPUT_FORMAT_CHANGED;
      }

      Integer index;
      if (timeoutUs < 0) {
        index = outputBufferAvailableIndexes.take();
      } else {
        index = outputBufferAvailableIndexes.poll(timeoutUs, TimeUnit.MICROSECONDS);
      }

      if (index == null) {
        return -1;
      }

      copyBufferInfo(outputBufferInfos[index], info);

      return index;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return -1;
    }
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
    inputBufferAvailableIndexes.add(index);

    if (isAsync) {
      // Signal input buffer availability.
      postFakeNativeEvent(EVENT_CALLBACK, CB_INPUT_AVAILABLE, index, null);
    }
  }

  private void makeOutputBufferAvailable(int index, BufferInfo info) {
    if (index < 0 || index >= outputBuffers.length) {
      throw new IndexOutOfBoundsException("Cannot make non-existent output buffer available.");
    }
    outputBuffers[index].clear();

    inputBuffers[index].position(info.offset).limit(info.offset + info.size);
    fakeCodec.process(inputBuffers[index], outputBuffers[index]);

    outputBufferInfos[index].flags = info.flags;
    outputBufferInfos[index].size = outputBuffers[index].position();
    outputBufferInfos[index].offset = info.offset;
    outputBufferInfos[index].presentationTimeUs = info.presentationTimeUs;
    outputBuffers[index].position(0).limit(outputBufferInfos[index].size);

    outputBufferAvailableIndexes.add(index);

    if (isAsync) {
      // Signal output buffer availability.
      postFakeNativeEvent(EVENT_CALLBACK, CB_OUTPUT_AVAILABLE, index, outputBufferInfos[index]);
    }
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

  /** Returns a default {@link MediaFormat} if not set via {@link #getOutputFormat()}. */
  @Implementation
  protected MediaFormat getOutputFormat() {
    if (outputFormat == null) {
      return new MediaFormat();
    }
    return outputFormat;
  }

  private static void copyBufferInfo(BufferInfo from, BufferInfo to) {
    to.set(from.offset, from.size, from.presentationTimeUs, from.flags);
  }

  private static MediaFormat recreateMediaFormatFromKeysValues(String[] keys, Object[] values) {
    MediaFormat mediaFormat = new MediaFormat();

    // This usage of `instanceof` is how API 29 `MediaFormat#getValueTypeForKey` works.
    for (int i = 0; i < keys.length; i++) {
      if (values[i] == null || values[i] instanceof ByteBuffer) {
        mediaFormat.setByteBuffer(keys[i], (ByteBuffer) values[i]);
      } else if (values[i] instanceof Integer) {
        mediaFormat.setInteger(keys[i], (Integer) values[i]);
      } else if (values[i] instanceof Long) {
        mediaFormat.setLong(keys[i], (Long) values[i]);
      } else if (values[i] instanceof Float) {
        mediaFormat.setFloat(keys[i], (Float) values[i]);
      } else if (values[i] instanceof String) {
        mediaFormat.setString(keys[i], (String) values[i]);
      } else {
        throw new IllegalArgumentException("Invalid value for key.");
      }
    }

    return mediaFormat;
  }

  /**
   * Configuration that can be supplied to {@link ShadowMediaCodec} to simulate actual
   * encoding/decoding.
   */
  public static final class CodecConfig {

    private final int inputBufferSize;
    private final int outputBufferSize;
    private final Codec codec;

    /**
     * @param inputBufferSize the size of the buffers offered as input to the codec.
     * @param outputBufferSize the size of the buffers offered as output from the codec.
     * @param codec should be able to map from input size -> output size
     */
    public CodecConfig(int inputBufferSize, int outputBufferSize, Codec codec) {
      this.inputBufferSize = inputBufferSize;
      this.outputBufferSize = outputBufferSize;

      this.codec = codec;
    }

    /**
     * A codec is implemented as part of the configuration to allow the {@link ShadowMediaCodec} to
     * simulate actual encoding/decoding. It's not expected for implementations to perform real
     * encoding/decoding, but to produce a output similar in size ratio to the expected codec..
     */
    public interface Codec {

      /** Move the bytes on the in buffer to the out buffer */
      void process(ByteBuffer in, ByteBuffer out);
    }
  }
}

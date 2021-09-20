package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;

import android.annotation.NonNull;
import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.media.MediaMuxer.Format;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/**
 * Implementation of {@link android.media.MediaMuxer} which directly passes input bytes to the
 * specified file, with no modification.
 */
@Implements(value = MediaMuxer.class, minSdk = LOLLIPOP)
public class ShadowMediaMuxer {
  // Maps between 'native' ids and corresponding output streams.
  private static final ConcurrentHashMap<Long, FileOutputStream> outputStreams =
      new ConcurrentHashMap<>();

  // Maps between 'native' ids and AtomicInteger objects tracking next track indices.
  private static final ConcurrentHashMap<Long, AtomicInteger> nextTrackIndices =
      new ConcurrentHashMap<>();

  // Maps between file descriptors and their original output stream.
  private static final ConcurrentHashMap<FileDescriptor, FileOutputStream> fdToStream =
      new ConcurrentHashMap<>();

  private static final Random random = new Random();

  // Keep in sync with MediaMuxer.java.
  private static final int MUXER_STATE_INITIALIZED = 0;

  @RealObject private MediaMuxer realMuxer;

  /**
   * Opens a FileOutputStream for the given path, and sets muxer state.
   *
   * <p>This needs to be shadowed, because the current MediaMuxer constructor opens a
   * RandomAccessFile, passes only the FileDescriptor along, and then closes the file. Since there
   * does not appear to be an easy way to go from FileDescriptor to a writeable stream in Java, this
   * method overrides that behavior to instead open and maintain a FileOutputStream.
   */
  @Implementation
  protected void __constructor__(@NonNull String path, @Format int format) throws IOException {
    if (path == null) {
      throw new IllegalArgumentException("path must not be null");
    }

    // Create a stream, and cache a mapping from file descriptor to stream.
    FileOutputStream stream = new FileOutputStream(path);
    FileDescriptor fd = stream.getFD();
    fdToStream.put(fd, stream);

    // Initialize the CloseGuard and last track index, since they are otherwise null and 0.
    CloseGuard guard = CloseGuard.get();
    ReflectionHelpers.setField(MediaMuxer.class, realMuxer, "mCloseGuard", guard);
    ReflectionHelpers.setField(MediaMuxer.class, realMuxer, "mLastTrackIndex", -1);

    // Pre-OREO jumps straight to nativeSetup inside the constructor.
    if (RuntimeEnvironment.getApiLevel() < O) {
      long nativeObject = nativeSetup(fd, format);
      ReflectionHelpers.setField(MediaMuxer.class, realMuxer, "mNativeObject", nativeObject);
      ReflectionHelpers.setField(MediaMuxer.class, realMuxer, "mState", MUXER_STATE_INITIALIZED);
      guard.open("release");
    } else {
      ReflectionHelpers.callInstanceMethod(
          MediaMuxer.class,
          realMuxer,
          "setUpMediaMuxer",
          ReflectionHelpers.ClassParameter.from(FileDescriptor.class, fd),
          ReflectionHelpers.ClassParameter.from(int.class, format));
    }
  }

  /**
   * Generates and returns an internal id to track the FileOutputStream corresponding to individual
   * MediaMuxer instances.
   */
  @Implementation
  protected static long nativeSetup(@NonNull FileDescriptor fd, int format) throws IOException {
    FileOutputStream outputStream = fdToStream.get(fd);

    long potentialKey;
    do {
      potentialKey = random.nextLong();
    } while (potentialKey == 0 || outputStreams.putIfAbsent(potentialKey, outputStream) != null);

    nextTrackIndices.put(potentialKey, new AtomicInteger(0));
    return potentialKey;
  }

  /** Returns an incremented track id for the associated muxer. */
  @Implementation
  protected static int nativeAddTrack(
      long nativeObject, @NonNull String[] keys, @NonNull Object[] values) {
    AtomicInteger nextTrackIndex = nextTrackIndices.get(nativeObject);
    if (nextTrackIndex == null) {
      throw new IllegalStateException("No next track index configured for key: " + nativeObject);
    }

    return nextTrackIndex.getAndIncrement();
  }

  /** Writes the given data to the FileOutputStream for the associated muxer. */
  @Implementation
  protected static void nativeWriteSampleData(
      long nativeObject,
      int trackIndex,
      @NonNull ByteBuffer byteBuf,
      int offset,
      int size,
      long presentationTimeUs,
      @MediaCodec.BufferFlag int flags) {
    byte[] bytes = new byte[size];
    int oldPosition = byteBuf.position();
    // The offset is the start-offset of the data in the buffer. We should use input offset for
    // byteBuf to read bytes, instead of byteBuf current offset.
    // See https://developer.android.com/reference/android/media/MediaCodec.BufferInfo#offset.
    byteBuf.position(offset);
    byteBuf.get(bytes, 0, size);
    byteBuf.position(oldPosition);

    try {
      getStream(nativeObject).write(bytes);
    } catch (IOException e) {
      throw new RuntimeException("Unable to write to temporary file.", e);
    }
  }

  /** Closes the FileOutputStream for the associated muxer. */
  @Implementation
  protected static void nativeStop(long nativeObject) {
    try {
      // Close the output stream.
      getStream(nativeObject).close();

      // Clear the stream from both internal caches.
      fdToStream.remove(outputStreams.remove(nativeObject).getFD());
    } catch (IOException e) {
      throw new RuntimeException("Unable to close temporary file.", e);
    }
  }

  private static FileOutputStream getStream(long streamKey) {
    FileOutputStream stream = outputStreams.get(streamKey);
    if (stream == null) {
      throw new IllegalStateException("No output stream configured for key: " + streamKey);
    }

    return stream;
  }
}

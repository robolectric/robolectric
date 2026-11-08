package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.google.common.io.Files;
import com.google.common.testing.GcFinalization;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.OsUtil;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public final class ShadowNativeAllocationRegistryTest {
  // Each bitmap is above glibc's maximum mmap threshold of 32 MB.
  private static final int BITMAP_WIDTH = 4096;
  private static final int BITMAP_HEIGHT = 2304;
  private static final long BITMAP_BYTES = 4L * BITMAP_WIDTH * BITMAP_HEIGHT; // 36 MB
  private static final int BITMAP_COUNT = 16;

  // TODO(hoisie): choose a different free function to test in V and above.
  @Config(maxSdk = UPSIDE_DOWN_CAKE)
  @Test
  public void applyFreeFunction_matrix() {
    WeakReference<Matrix> weakMatrix = new WeakReference<>(newMatrix());
    // Invokes 'applyFreeFunction' when the matrix is GC'd.
    GcFinalization.awaitClear(weakMatrix);
  }

  // Creates a new Matrix as a local variable, which is eligible for GC when it goes out
  // of scope.
  private Matrix newMatrix() {
    Matrix matrix = new Matrix();
    long pointer = reflector(MatrixReflector.class, matrix).getNativeInstance();
    long freeFunction = reflector(MatrixReflector.class).nGetNativeFinalizer();
    assertThat(pointer).isNotEqualTo(0);
    assertThat(freeFunction).isNotEqualTo(0);
    return matrix;
  }

  /**
   * Checks that the native pixel memory of collected bitmaps is actually freed.
   *
   * <p>This measures the virtual size of the process rather than any bookkeeping in Robolectric, so
   * it only passes if the real native free function runs. Each bitmap is larger than glibc's
   * maximum mmap threshold (32 MB), so its pixels are mmapped and unmapped as soon as they are
   * freed. The pixels are never touched, so the test uses no physical memory. Half of the
   * allocation is allowed to remain because other threads can reserve 64 MB malloc arenas.
   */
  @Test
  public void bitmapNativeAllocations_areFreedWhenCollected() {
    assume().that(OsUtil.isLinux()).isTrue();
    // Loads the native graphics libraries.
    Bitmap first = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    // Frees the bitmaps of earlier tests, which would otherwise offset the measurements.
    GcFinalization.awaitFullGc();
    long before = virtualMemoryBytes();

    List<WeakReference<Bitmap>> bitmaps = createLargeBitmaps(BITMAP_COUNT);
    long allocated = virtualMemoryBytes() - before;
    assertThat(allocated).isAtLeast(BITMAP_COUNT * BITMAP_BYTES);

    for (WeakReference<Bitmap> bitmap : bitmaps) {
      GcFinalization.awaitClear(bitmap);
    }
    // The registry frees them on the cleaner thread after they are collected.
    try {
      GcFinalization.awaitDone(() -> virtualMemoryBytes() - before < allocated / 2);
    } catch (RuntimeException timedOut) {
      long leaked = virtualMemoryBytes() - before;
      throw new AssertionError(
          (leaked >> 20)
              + " MB of "
              + (allocated >> 20)
              + " MB was not freed after the bitmaps were collected",
          timedOut);
    }
    assertThat(first.getWidth()).isEqualTo(1);
  }

  private static List<WeakReference<Bitmap>> createLargeBitmaps(int count) {
    List<WeakReference<Bitmap>> references = new ArrayList<>();
    // Keeps the bitmaps reachable until all of them are created.
    List<Bitmap> bitmaps = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
      bitmaps.add(bitmap);
      references.add(new WeakReference<>(bitmap));
    }
    Reference.reachabilityFence(bitmaps);
    return references;
  }

  /** Returns the virtual size of this process, from VmSize in /proc/self/status. */
  private static long virtualMemoryBytes() {
    try {
      String status = Files.asCharSource(new File("/proc/self/status"), UTF_8).read();
      return Long.parseLong(status.replaceAll("(?s).*VmSize:\\s*(\\d+).*", "$1")) * 1024;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @ForType(Matrix.class)
  interface MatrixReflector {
    @Accessor("native_instance")
    long getNativeInstance();

    @Static
    long nGetNativeFinalizer();
  }
}

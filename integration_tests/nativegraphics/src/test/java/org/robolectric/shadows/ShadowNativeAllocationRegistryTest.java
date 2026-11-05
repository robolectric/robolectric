package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import com.google.common.testing.GcFinalization;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public final class ShadowNativeAllocationRegistryTest {
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
   * Checks that the native allocations of collected bitmaps are freed, using the number of live
   * allocations with Bitmap's free function, so a single leaked allocation fails the test.
   */
  @Test
  public void bitmapNativeAllocations_areFreedWhenCollected() {
    // Loads the native graphics libraries.
    Bitmap first = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    long freeFunction = reflector(BitmapReflector.class).nativeGetNativeFinalizer();
    // Frees the bitmaps of earlier tests, which would otherwise offset the count.
    GcFinalization.awaitFullGc();
    long before = liveAllocations(freeFunction);

    List<WeakReference<Bitmap>> bitmaps = createBitmaps(100);
    assertThat(liveAllocations(freeFunction)).isEqualTo(before + 100);

    for (WeakReference<Bitmap> bitmap : bitmaps) {
      GcFinalization.awaitClear(bitmap);
    }
    // The registry frees them on the cleaner thread after they are collected.
    try {
      GcFinalization.awaitDone(() -> liveAllocations(freeFunction) <= before);
    } catch (RuntimeException timedOut) {
      throw new AssertionError(
          (liveAllocations(freeFunction) - before)
              + " of 100 collected bitmaps leaked their native allocation",
          timedOut);
    }
    assertThat(first.getWidth()).isEqualTo(1);
  }

  private static long liveAllocations(long freeFunction) {
    return ShadowNativeAllocationRegistry.getMetricsCompat().getOrDefault(freeFunction, 0L);
  }

  private static List<WeakReference<Bitmap>> createBitmaps(int count) {
    List<Bitmap> bitmaps = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Bitmap bitmap = Bitmap.createBitmap(97, 89, Bitmap.Config.ARGB_8888);
      bitmap.eraseColor(Color.BLUE);
      bitmaps.add(bitmap);
    }
    List<WeakReference<Bitmap>> references = new ArrayList<>();
    for (Bitmap bitmap : bitmaps) {
      references.add(new WeakReference<>(bitmap));
    }
    return references;
  }

  @ForType(Bitmap.class)
  interface BitmapReflector {
    @Static
    long nativeGetNativeFinalizer();
  }

  @ForType(Matrix.class)
  interface MatrixReflector {
    @Accessor("native_instance")
    long getNativeInstance();

    @Static
    long nGetNativeFinalizer();
  }
}

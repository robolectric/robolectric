package org.robolectric.integrationtests.nativegraphics;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import com.google.common.testing.GcFinalization;
import java.lang.ref.WeakReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.U;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public final class ShadowNativeAllocationRegistryTest {
  // TODO(hoisie): choose a different free function to test in V and above.
  @Config(maxSdk = U.SDK_INT)
  @Test
  public void applyFreeFunction_matrix() throws Exception {
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

  @Config(sdk = S) // No need to re-run on multiple SDK levels
  @Test
  public void nativeAllocationRegistryStressTest() {
    for (int i = 0; i < 10_000; i++) {
      Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
      bitmap.eraseColor(Color.BLUE);
      if (i % 100 == 0) {
        System.gc();
      }
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

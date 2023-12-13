package org.robolectric.integrationtests.nativegraphics;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import libcore.util.NativeAllocationRegistry;
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
  @Test
  public void applyFreeFunction_matrix() {
    Matrix matrix = new Matrix();
    long pointer = reflector(MatrixReflector.class, matrix).getNativeInstance();
    long freeFunction = reflector(MatrixReflector.class).nGetNativeFinalizer();
    assertThat(pointer).isNotEqualTo(0);
    assertThat(freeFunction).isNotEqualTo(0);
    NativeAllocationRegistry.applyFreeFunction(freeFunction, pointer);
    reflector(MatrixReflector.class, matrix).setNativeInstance(0); // Zero the pointer
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

    @Accessor("native_instance")
    void setNativeInstance(long value);

    @Static
    long nGetNativeFinalizer();
  }
}

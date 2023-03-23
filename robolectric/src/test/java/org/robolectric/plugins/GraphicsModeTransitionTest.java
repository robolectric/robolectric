package org.robolectric.plugins;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.GraphicsMode.Mode.LEGACY;
import static org.robolectric.annotation.GraphicsMode.Mode.NATIVE;

import android.graphics.Matrix;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLegacyMatrix;
import org.robolectric.shadows.ShadowMatrix;
import org.robolectric.shadows.ShadowNativeMatrix;

/**
 * Tests methods that cause transitions to different graphics modes. This is to verify that shadow
 * invalidation of graphics shadows occurs when the graphics mode changes.
 *
 * <p>Method order is important to ensure consistent transitions between LEGACY -> NATIVE -> LEGACY
 * graphics.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config(sdk = TIRAMISU)
public class GraphicsModeTransitionTest {
  @GraphicsMode(LEGACY)
  @Test
  public void test1Legacy() {
    ShadowMatrix shadowMatrix = Shadow.extract(new Matrix());
    assertThat(shadowMatrix).isInstanceOf(ShadowLegacyMatrix.class);
  }

  @GraphicsMode(NATIVE)
  @Test
  public void test2NativeAfterLegacy() {
    ShadowMatrix shadowMatrix = Shadow.extract(new Matrix());
    assertThat(shadowMatrix).isInstanceOf(ShadowNativeMatrix.class);
  }

  @GraphicsMode(LEGACY)
  @Test
  public void test3LegacyAfterNative() {
    ShadowMatrix shadowMatrix = Shadow.extract(new Matrix());
    assertThat(shadowMatrix).isInstanceOf(ShadowLegacyMatrix.class);
  }
}

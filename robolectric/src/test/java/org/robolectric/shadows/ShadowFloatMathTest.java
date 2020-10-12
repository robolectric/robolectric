package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static com.google.common.truth.Truth.assertThat;

import android.util.FloatMath;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link FloatMath}. On SDKs < 23, {@link FloatMath} was implemented using native
 * methods.
 */
@Config(minSdk = JELLY_BEAN)
@RunWith(AndroidJUnit4.class)
public class ShadowFloatMathTest {

  @Test
  public void testFloor() {
    assertThat(FloatMath.floor(1.1f)).isEqualTo(1);
  }
}

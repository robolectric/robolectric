package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.view.ViewConfiguration;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowViewConfigurationTest {

  private Application context;
  private ViewConfiguration viewConfiguration;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    viewConfiguration = ViewConfiguration.get(context);
  }

  @Test
  public void methodsShouldReturnAndroidConstants() {
    // Most of the constants here are private statics from ViewConfiguration circa Jelly Bean:
    // https://cs.android.com/android/platform/superproject/+/android-4.1.1_r1:frameworks/base/core/java/android/view/ViewConfiguration.java
    final int expectedScrollBarSize = RuntimeEnvironment.getApiLevel() >= O_MR1 ? 4 : 10;
    assertEquals(expectedScrollBarSize, ViewConfiguration.getScrollBarSize());
    assertEquals(250, ViewConfiguration.getScrollBarFadeDuration());
    assertEquals(300, ViewConfiguration.getScrollDefaultDelay());
    assertEquals(12, ViewConfiguration.getFadingEdgeLength());
    assertEquals(125, ViewConfiguration.getPressedStateDuration());
    assertEquals(500, ViewConfiguration.getLongPressTimeout());
    assertEquals(115, ViewConfiguration.getTapTimeout());
    assertEquals(500, ViewConfiguration.getJumpTapTimeout());
    assertEquals(300, ViewConfiguration.getDoubleTapTimeout());
    assertEquals(12, ViewConfiguration.getEdgeSlop());
    assertEquals(16, ViewConfiguration.getTouchSlop());
    assertEquals(16, ViewConfiguration.getWindowTouchSlop());
    assertEquals(50, ViewConfiguration.getMinimumFlingVelocity());
    assertEquals(8000, ViewConfiguration.getMaximumFlingVelocity());
    assertEquals(480 * 800 * 4, ViewConfiguration.getMaximumDrawingCacheSize());
    assertEquals(3000, ViewConfiguration.getZoomControlsTimeout());
    assertEquals(500, ViewConfiguration.getGlobalActionKeyTimeout());
    assertThat(ViewConfiguration.getScrollFriction()).isEqualTo(0.015f);

    assertThat(context.getResources().getDisplayMetrics().density).isEqualTo(1f);

    assertEquals(expectedScrollBarSize, viewConfiguration.getScaledScrollBarSize());
    assertEquals(12, viewConfiguration.getScaledFadingEdgeLength());
    assertEquals(12, viewConfiguration.getScaledEdgeSlop());
    assertEquals(16, viewConfiguration.getScaledTouchSlop());
    assertEquals(32, viewConfiguration.getScaledPagingTouchSlop());
    assertEquals(100, viewConfiguration.getScaledDoubleTapSlop());
    assertEquals(16, viewConfiguration.getScaledWindowTouchSlop());
    assertEquals(50, viewConfiguration.getScaledMinimumFlingVelocity());
    assertEquals(8000, viewConfiguration.getScaledMaximumFlingVelocity());
    // The min value of getScaledMaximumDrawingCacheSize is 480 * 800 * 4.
    assertEquals(480 * 800 * 4, viewConfiguration.getScaledMaximumDrawingCacheSize());
    assertThat(viewConfiguration.isFadingMarqueeEnabled()).isFalse();
    assertThat(viewConfiguration.getScaledOverflingDistance()).isEqualTo(6);
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      assertThat(viewConfiguration.getScaledMinimumScalingSpan()).isEqualTo(170);
    }
  }

  @Test
  @Config(qualifiers = "hdpi")
  public void methodsShouldReturnScaledAndroidConstantsDependingOnPixelDensity() {
    // Most of the constants here are private statics from ViewConfiguration circa Jelly Bean:
    // https://cs.android.com/android/platform/superproject/+/android-4.1.1_r1:frameworks/base/core/java/android/view/ViewConfiguration.java
    // They are multiplied by the scaling factor 1.5 for HDPI.
    final int expectedScaledScrollBarSize = RuntimeEnvironment.getApiLevel() >= O_MR1 ? 6 : 15;
    assertEquals(expectedScaledScrollBarSize, viewConfiguration.getScaledScrollBarSize());
    assertEquals(18, viewConfiguration.getScaledFadingEdgeLength());
    assertEquals(18, viewConfiguration.getScaledEdgeSlop());
    assertEquals(24, viewConfiguration.getScaledTouchSlop());
    assertEquals(48, viewConfiguration.getScaledPagingTouchSlop());
    assertEquals(150, viewConfiguration.getScaledDoubleTapSlop());
    assertEquals(24, viewConfiguration.getScaledWindowTouchSlop());
    assertEquals(75, viewConfiguration.getScaledMinimumFlingVelocity());
    assertEquals(12000, viewConfiguration.getScaledMaximumFlingVelocity());
    assertThat(viewConfiguration.getScaledOverflingDistance()).isEqualTo(9);
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      assertThat(viewConfiguration.getScaledMinimumScalingSpan()).isEqualTo(255);
    }
  }

  @Test
  public void testHasPermanentMenuKey() {
    assertThat(viewConfiguration.hasPermanentMenuKey()).isTrue();

    ShadowViewConfiguration shadowViewConfiguration = shadowOf(viewConfiguration);
    shadowViewConfiguration.setHasPermanentMenuKey(false);
    assertThat(viewConfiguration.hasPermanentMenuKey()).isFalse();
  }

  @Config(qualifiers = "w420dp-h800dp-xxxhdpi")
  @Test
  public void getScaledMaximumFlingVelocity_scalesWithDisplaySize() {
    int expected = 4 * (4 * 420) * (4 * 800);
    assertThat(viewConfiguration.getScaledMaximumDrawingCacheSize()).isEqualTo(expected);
  }

  @Config(qualifiers = "w100dp-h500dp")
  @Test
  public void getScaledMaximumFlingVelocity_minValue() {
    int expected = 480 * 800 * 4; // The min value
    assertThat(viewConfiguration.getScaledMaximumDrawingCacheSize()).isEqualTo(expected);
  }

  @Config(minSdk = Q, qualifiers = "w600dp-h800dp")
  @Test
  public void getScaledMinimumScalingSpan_largeScreen() {
    assertThat(viewConfiguration.getScaledMinimumScalingSpan()).isEqualTo(202);
  }

  @Config(minSdk = Q)
  @Test
  public void getScaledMinimumScalingSpan_usePreviousBug() {
    System.setProperty("robolectric.useRealMinScalingSpan", "false");
    ShadowViewConfiguration.reset(); // clear the static cache
    try {
      ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
      assertThat(viewConfiguration.getScaledMinimumScalingSpan()).isEqualTo(0);
    } finally {
      System.clearProperty("robolectric.useRealMinScalingSpan");
    }
  }
}

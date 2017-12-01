package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.view.ViewConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ShadowViewConfigurationTest {

  @Test
  public void methodsShouldReturnAndroidConstants() {
    ViewConfiguration viewConfiguration = ViewConfiguration.get(RuntimeEnvironment.application);

    assertEquals(10, ViewConfiguration.getScrollBarSize());
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
    assertEquals(4000, ViewConfiguration.getMaximumFlingVelocity());
    assertEquals(320 * 480 * 4, ViewConfiguration.getMaximumDrawingCacheSize());
    assertEquals(3000, ViewConfiguration.getZoomControlsTimeout());
    assertEquals(500, ViewConfiguration.getGlobalActionKeyTimeout());
    assertThat(ViewConfiguration.getScrollFriction()).isEqualTo(0.015f);

    assertThat(RuntimeEnvironment.application.getResources().getDisplayMetrics().density)
        .isEqualTo(1f);

    assertEquals(10, viewConfiguration.getScaledScrollBarSize());
    assertEquals(12, viewConfiguration.getScaledFadingEdgeLength());
    assertEquals(12, viewConfiguration.getScaledEdgeSlop());
    assertEquals(16, viewConfiguration.getScaledTouchSlop());
    assertEquals(32, viewConfiguration.getScaledPagingTouchSlop());
    assertEquals(100, viewConfiguration.getScaledDoubleTapSlop());
    assertEquals(16, viewConfiguration.getScaledWindowTouchSlop());
    assertEquals(50, viewConfiguration.getScaledMinimumFlingVelocity());
    assertEquals(4000, viewConfiguration.getScaledMaximumFlingVelocity());
  }

  @Test
  public void methodsShouldReturnScaledAndroidConstantsDependingOnPixelDensity() {
    shadowOf(RuntimeEnvironment.application.getResources()).setDensity(1.5f);
    ViewConfiguration viewConfiguration = ViewConfiguration.get(RuntimeEnvironment.application);

    assertEquals(15, viewConfiguration.getScaledScrollBarSize());
    assertEquals(18, viewConfiguration.getScaledFadingEdgeLength());
    assertEquals(18, viewConfiguration.getScaledEdgeSlop());
    assertEquals(24, viewConfiguration.getScaledTouchSlop());
    assertEquals(48, viewConfiguration.getScaledPagingTouchSlop());
    assertEquals(150, viewConfiguration.getScaledDoubleTapSlop());
    assertEquals(24, viewConfiguration.getScaledWindowTouchSlop());
    assertEquals(75, viewConfiguration.getScaledMinimumFlingVelocity());
    assertEquals(6000, viewConfiguration.getScaledMaximumFlingVelocity());
  }

  @Test
  public void testHasPermanentMenuKey() throws Exception {
    ViewConfiguration viewConfiguration = ViewConfiguration.get(RuntimeEnvironment.application);
    assertThat(viewConfiguration.hasPermanentMenuKey()).isFalse();

    ShadowViewConfiguration shadowViewConfiguration = shadowOf(viewConfiguration);
    shadowViewConfiguration.setHasPermanentMenuKey(true);
    assertThat(viewConfiguration.hasPermanentMenuKey()).isTrue();
  }
}

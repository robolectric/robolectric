package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.view.ViewConfiguration;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;


@RunWith(WithTestDefaultsRunner.class)
public class ViewConfigurationTest {

    @Test
    public void methodsShouldReturnAndroidConstants() {
        Activity context = new Activity();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);

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
        assertEquals(0.015f, ViewConfiguration.getScrollFriction());

        assertEquals(1f, context.getResources().getDisplayMetrics().density);

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
        Activity context = new Activity();
        shadowOf(context.getResources()).setDensity(1.5f);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);

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

}

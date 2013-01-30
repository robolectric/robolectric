/*
 * Portions of this code came from frameworks/base/core/java/android/view/ViewConfiguration.java,
 * which contains the following license text:
 *
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewConfiguration.class)
public class ShadowViewConfiguration {

    private static final int SCROLL_BAR_SIZE = 10;
    private static final int SCROLL_BAR_FADE_DURATION = 250;
    private static final int SCROLL_BAR_DEFAULT_DELAY = 300;
    private static final int FADING_EDGE_LENGTH = 12;
    private static final int PRESSED_STATE_DURATION = 125;
    private static final int LONG_PRESS_TIMEOUT = 500;
    private static final int GLOBAL_ACTIONS_KEY_TIMEOUT = 500;
    private static final int TAP_TIMEOUT = 115;
    private static final int JUMP_TAP_TIMEOUT = 500;
    private static final int DOUBLE_TAP_TIMEOUT = 300;
    private static final int ZOOM_CONTROLS_TIMEOUT = 3000;
    private static final int EDGE_SLOP = 12;
    private static final int TOUCH_SLOP = 16;
    private static final int PAGING_TOUCH_SLOP = TOUCH_SLOP * 2;
    private static final int DOUBLE_TAP_SLOP = 100;
    private static final int WINDOW_TOUCH_SLOP = 16;
    private static final int MINIMUM_FLING_VELOCITY = 50;
    private static final int MAXIMUM_FLING_VELOCITY = 4000;
    private static final int MAXIMUM_DRAWING_CACHE_SIZE = 320 * 480 * 4;
    private static float SCROLL_FRICTION = 0.015f;
    private static final int OVERSCROLL_DISTANCE = 0;
    private static final int OVERFLING_DISTANCE = 4;

    private int edgeSlop;
    private int fadingEdgeLength;
    private int minimumFlingVelocity;
    private int maximumFlingVelocity;
    private int scrollbarSize;
    private int touchSlop;
    private int pagingTouchSlop;
    private int doubleTapSlop;
    private int windowTouchSlop;

    @RealObject
    private ViewConfiguration realViewConfiguration;

    private void setup(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density;

        edgeSlop = (int) (density * EDGE_SLOP + 0.5f);
        fadingEdgeLength = (int) (density * FADING_EDGE_LENGTH + 0.5f);
        minimumFlingVelocity = (int) (density * MINIMUM_FLING_VELOCITY + 0.5f);
        maximumFlingVelocity = (int) (density * MAXIMUM_FLING_VELOCITY + 0.5f);
        scrollbarSize = (int) (density * SCROLL_BAR_SIZE + 0.5f);
        touchSlop = (int) (density * TOUCH_SLOP + 0.5f);
        pagingTouchSlop = (int) (density * PAGING_TOUCH_SLOP + 0.5f);
        doubleTapSlop = (int) (density * DOUBLE_TAP_SLOP + 0.5f);
        windowTouchSlop = (int) (density * WINDOW_TOUCH_SLOP + 0.5f);
    }

    @Implementation
    public static ViewConfiguration get(Context context) {
        ViewConfiguration viewConfiguration = Robolectric.newInstanceOf(ViewConfiguration.class);
        shadowOf(viewConfiguration).setup(context);
        return viewConfiguration;
    }

    @Implementation
    public static int getScrollBarSize() {
        return SCROLL_BAR_SIZE;
    }

    @Implementation
    public int getScaledScrollBarSize() {
        return scrollbarSize;
    }

    @Implementation
    public static int getScrollBarFadeDuration() {
        return SCROLL_BAR_FADE_DURATION;
    }

    @Implementation
    public static int getScrollDefaultDelay() {
        return SCROLL_BAR_DEFAULT_DELAY;
    }

    @Implementation
    public static int getFadingEdgeLength() {
        return FADING_EDGE_LENGTH;
    }

    @Implementation
    public int getScaledFadingEdgeLength() {
        return fadingEdgeLength;
    }

    @Implementation
    public static int getPressedStateDuration() {
        return PRESSED_STATE_DURATION;
    }

    @Implementation
    public static int getLongPressTimeout() {
        return LONG_PRESS_TIMEOUT;
    }

    @Implementation
    public static int getTapTimeout() {
        return TAP_TIMEOUT;
    }

    @Implementation
    public static int getJumpTapTimeout() {
        return JUMP_TAP_TIMEOUT;
    }

    @Implementation
    public static int getDoubleTapTimeout() {
        return DOUBLE_TAP_TIMEOUT;
    }

    @Implementation
    public static int getEdgeSlop() {
        return EDGE_SLOP;
    }

    @Implementation
    public int getScaledEdgeSlop() {
        return edgeSlop;
    }

    @Implementation
    public static int getTouchSlop() {
        return TOUCH_SLOP;
    }

    @Implementation
    public int getScaledTouchSlop() {
        return touchSlop;
    }

    @Implementation
    public int getScaledPagingTouchSlop() {
        return pagingTouchSlop;
    }

    @Implementation
    public int getScaledDoubleTapSlop() {
        return doubleTapSlop;
    }

    @Implementation
    public static int getWindowTouchSlop() {
        return WINDOW_TOUCH_SLOP;
    }

    @Implementation
    public int getScaledWindowTouchSlop() {
        return windowTouchSlop;
    }

    @Implementation
    public static int getMinimumFlingVelocity() {
        return MINIMUM_FLING_VELOCITY;
    }

    @Implementation
    public int getScaledMinimumFlingVelocity() {
        return minimumFlingVelocity;
    }

    @Implementation
    public static int getMaximumFlingVelocity() {
        return MAXIMUM_FLING_VELOCITY;
    }

    @Implementation
    public int getScaledMaximumFlingVelocity() {
        return maximumFlingVelocity;
    }

    @Implementation
    public static int getMaximumDrawingCacheSize() {
        return MAXIMUM_DRAWING_CACHE_SIZE;
    }

    @Implementation
    public static long getZoomControlsTimeout() {
        return ZOOM_CONTROLS_TIMEOUT;
    }

    @Implementation
    public static long getGlobalActionKeyTimeout() {
        return GLOBAL_ACTIONS_KEY_TIMEOUT;
    }

    @Implementation
    public static float getScrollFriction() {
        return SCROLL_FRICTION;
    }

}

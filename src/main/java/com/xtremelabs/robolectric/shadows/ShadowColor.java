package com.xtremelabs.robolectric.shadows;

import android.graphics.Color;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Color.class)
public class ShadowColor {
    @Implementation
    public static int rgb(int red, int green, int blue) {
        return argb(0xff, red, green, blue);
    }

    @Implementation
    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
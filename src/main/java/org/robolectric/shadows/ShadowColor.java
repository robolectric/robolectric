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

    @Implementation // copied from Android
    public static int parseColor(String colorString) {
        if (colorString.charAt(0) == '#') {
            // Use a long to avoid rollovers on #ffXXXXXX
            long color = Long.parseLong(colorString.substring(1), 16);
            if (colorString.length() == 7) {
                // Set the alpha value
                color |= 0x00000000ff000000;
            } else if (colorString.length() != 9) {
                throw new IllegalArgumentException("Unknown color");
            }
            return (int) color;
        } else {
            // we didn't copy this else case
        }
        throw new IllegalArgumentException("Unknown color");
    }
}